/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.rest;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.analytics.domain.PrimeCostReport;
import aros.services.rms.core.analytics.domain.PrimeCostReport.CogsBreakdown;
import aros.services.rms.core.analytics.domain.PrimeCostReport.CogsCategory;
import aros.services.rms.core.analytics.domain.PrimeCostReport.LaborArea;
import aros.services.rms.core.analytics.domain.PrimeCostReport.LaborBreakdown;
import aros.services.rms.core.analytics.domain.PrimeCostReport.Margins;
import aros.services.rms.core.analytics.domain.PrimeCostReport.Period;
import aros.services.rms.core.analytics.domain.PrimeCostReport.PrimeCostSeries;
import aros.services.rms.core.analytics.domain.port.in.GetPrimeCostUseCase;
import aros.services.rms.core.analytics.infrastructure.rest.mapper.PrimeCostReportMapper;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.infraestructure.image.storage.local.LocalResourceConfig;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Web MVC slice tests for {@link PrimeCostController}. Covers auth, validation, and response shape
 * for GET /api/v1/analytics/prime-cost.
 */
@WebMvcTest(
    value = PrimeCostController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = LocalResourceConfig.class))
@Import(PrimeCostControllerWebMvcTest.TestSecurityConfig.class)
class PrimeCostControllerWebMvcTest {

  private static final String URL = "/api/v1/analytics/prime-cost";
  private static final Currency COP = Currency.getInstance("COP");

  @Autowired private MockMvc mockMvc;

  @MockitoBean private GetPrimeCostUseCase getPrimeCostUseCase;
  @MockitoBean private PrimeCostReportMapper primeCostReportMapper;
  @MockitoBean private JwtDecoder jwtDecoder;

  @org.springframework.boot.test.context.TestConfiguration
  @EnableMethodSecurity
  static class TestSecurityConfig {
    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
      http.csrf(csrf -> csrf.disable())
          .sessionManagement(
              session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
          .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
      return http.build();
    }
  }

  // ---------------------------------------------------------------------------
  // 200: returns prime cost report
  // ---------------------------------------------------------------------------

  @Test
  void should_allow_admin_to_get_prime_cost() throws Exception {
    PrimeCostReport report = sampleReport();
    when(getPrimeCostUseCase.execute("monthly", "2026-01", "2026-07")).thenReturn(report);

    mockMvc
        .perform(
            get(URL)
                .param("bucket", "monthly")
                .param("from", "2026-01")
                .param("to", "2026-07")
                .with(adminJwt()))
        .andExpect(status().isOk());
  }

  // ---------------------------------------------------------------------------
  // 401: missing auth
  // ---------------------------------------------------------------------------

  @Test
  void should_reject_unauthenticated() throws Exception {
    mockMvc
        .perform(
            get(URL).param("bucket", "monthly").param("from", "2026-01").param("to", "2026-07"))
        .andExpect(status().isUnauthorized());
  }

  // ---------------------------------------------------------------------------
  // 403: wrong role
  // ---------------------------------------------------------------------------

  @Test
  void should_reject_non_admin() throws Exception {
    mockMvc
        .perform(
            get(URL)
                .param("bucket", "monthly")
                .param("from", "2026-01")
                .param("to", "2026-07")
                .with(workerJwt()))
        .andExpect(status().isForbidden());
  }

  // ---------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------

  private static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor adminJwt() {
    return jwt()
        .jwt(builder -> builder.subject("admin@test.com").claim("role", "ADMIN"))
        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
  }

  private static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor workerJwt() {
    return jwt()
        .jwt(builder -> builder.subject("worker@test.com").claim("role", "WORKER"))
        .authorities(new SimpleGrantedAuthority("ROLE_USER"));
  }

  private static PrimeCostReport sampleReport() {
    Money zero = Money.zero(COP);
    Money cogsFood = new Money(new BigDecimal("30000000.00"), COP);
    Money cogsBev = new Money(new BigDecimal("8000000.00"), COP);
    Money cogsAlc = new Money(new BigDecimal("4000000.00"), COP);
    Money totalCogs = cogsFood.plus(cogsBev).plus(cogsAlc);
    Money netSales = new Money(new BigDecimal("125000000.00"), COP);
    Money laborFoh = new Money(new BigDecimal("18000000.00"), COP);
    Money laborBoh = new Money(new BigDecimal("17000000.00"), COP);
    Money laborTotal = laborFoh.plus(laborBoh);
    Money primeCost = totalCogs.plus(laborTotal);

    return new PrimeCostReport(
        new Period("monthly", "2026-01", "2026-07", List.of("2026-01", "2026-02")),
        List.of(
            new PrimeCostSeries(
                "2026-07",
                netSales,
                netSales,
                zero,
                zero,
                new CogsBreakdown(
                    totalCogs,
                    List.of(
                        new CogsCategory("FOOD", cogsFood, new BigDecimal("71.43")),
                        new CogsCategory("BEVERAGE", cogsBev, new BigDecimal("19.05")),
                        new CogsCategory("ALCOHOL", cogsAlc, new BigDecimal("9.52")))),
                new LaborBreakdown(
                    laborTotal,
                    List.of(
                        new LaborArea("FOH", laborFoh, new BigDecimal("51.43")),
                        new LaborArea("BOH", laborBoh, new BigDecimal("48.57")))),
                primeCost,
                new BigDecimal("61.60"),
                new Margins(new BigDecimal("66.40"), new BigDecimal("4.20")),
                "FULL")),
        "FULL",
        List.of());
  }
}
