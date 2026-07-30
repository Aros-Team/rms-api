/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.rest;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.analytics.domain.BcgQuadrant;
import aros.services.rms.core.analytics.domain.MenuEngineeringReport;
import aros.services.rms.core.analytics.domain.MenuEngineeringReport.CacheStatus;
import aros.services.rms.core.analytics.domain.MenuEngineeringReport.MedianInfo;
import aros.services.rms.core.analytics.domain.MenuEngineeringReport.MenuItemSummary;
import aros.services.rms.core.analytics.domain.MenuEngineeringReport.PeriodInfo;
import aros.services.rms.core.analytics.domain.port.in.GetMenuEngineeringUseCase;
import aros.services.rms.core.analytics.infrastructure.rest.dto.MenuEngineeringReportResponse;
import aros.services.rms.core.analytics.infrastructure.rest.dto.MoneyDto;
import aros.services.rms.core.analytics.infrastructure.rest.mapper.MenuEngineeringReportMapper;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.infraestructure.image.storage.local.LocalResourceConfig;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
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
 * Web MVC slice tests for {@link MenuEngineeringController}. Covers auth, validation, and response
 * shape for GET /api/v1/analytics/menu-engineering.
 */
@WebMvcTest(
    value = MenuEngineeringController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = LocalResourceConfig.class))
@Import(MenuEngineeringControllerWebMvcTest.TestSecurityConfig.class)
class MenuEngineeringControllerWebMvcTest {

  private static final String URL = "/api/v1/analytics/menu-engineering";
  private static final Currency COP = Currency.getInstance("COP");

  @Autowired private MockMvc mockMvc;

  @MockitoBean private GetMenuEngineeringUseCase getMenuEngineeringUseCase;
  @MockitoBean private MenuEngineeringReportMapper menuEngineeringReportMapper;
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
  // 200: returns menu engineering report
  // ---------------------------------------------------------------------------

  @Test
  void should_allow_admin_to_get_report() throws Exception {
    MenuEngineeringReport report = sampleReport();
    when(getMenuEngineeringUseCase.execute("monthly", "2026-07", "2026-07", null))
        .thenReturn(report);
    when(menuEngineeringReportMapper.toResponse(ArgumentMatchers.any(MenuEngineeringReport.class)))
        .thenReturn(sampleReportResponse());

    mockMvc
        .perform(
            get(URL)
                .param("bucket", "monthly")
                .param("from", "2026-07")
                .param("to", "2026-07")
                .with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items[0].avgOptionCost").exists())
        .andExpect(jsonPath("$.items[0].effectiveCost").exists());
  }

  // ---------------------------------------------------------------------------
  // 200: with categoryId filter
  // ---------------------------------------------------------------------------

  @Test
  void should_accept_optional_category_id() throws Exception {
    when(getMenuEngineeringUseCase.execute("monthly", "2026-07", "2026-07", 1L))
        .thenReturn(sampleReport());

    mockMvc
        .perform(
            get(URL)
                .param("bucket", "monthly")
                .param("from", "2026-07")
                .param("to", "2026-07")
                .param("categoryId", "1")
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
            get(URL).param("bucket", "monthly").param("from", "2026-07").param("to", "2026-07"))
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
                .param("from", "2026-07")
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

  private static MenuEngineeringReport sampleReport() {
    MenuItemSummary item =
        new MenuItemSummary(
            42L,
            "Lomo en salsa",
            3L,
            "Platos fuertes",
            320,
            Money.of("9600000.00", COP),
            Money.of("3200000.00", COP),
            Money.of("0.00", COP),
            Money.of("3200000.00", COP),
            Money.of("20000.00", COP),
            Money.of("6400000.00", COP),
            BcgQuadrant.STAR);

    return new MenuEngineeringReport(
        new PeriodInfo("monthly", "2026-07", "2026-07", List.of("2026-07")),
        new MedianInfo(142, Money.of("8500.00", COP)),
        List.of(item),
        new CacheStatus(Instant.parse("2026-07-17T02:00:00Z"), "v17", 86400),
        "FULL",
        List.of());
  }

  private static MenuEngineeringReportResponse sampleReportResponse() {
    MoneyDto money = MoneyDto.builder().amount("0.00").currency("COP").build();
    MenuEngineeringReportResponse.MenuItemResponse item =
        MenuEngineeringReportResponse.MenuItemResponse.builder()
            .productId(42L)
            .productName("Lomo en salsa")
            .categoryId(3L)
            .categoryName("Platos fuertes")
            .unitsSold(320)
            .revenue(money)
            .recipeCost(money)
            .avgOptionCost(money)
            .effectiveCost(money)
            .grossProfitPerUnit(money)
            .totalContribution(money)
            .quadrant("STAR")
            .build();
    return MenuEngineeringReportResponse.builder()
        .items(List.of(item))
        .dataCompleteness("FULL")
        .build();
  }
}
