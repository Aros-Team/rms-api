/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.rest;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.analytics.domain.OperationsReport;
import aros.services.rms.core.analytics.domain.OperationsReport.LaborShare;
import aros.services.rms.core.analytics.domain.OperationsReport.PeriodInfo;
import aros.services.rms.core.analytics.domain.OperationsReport.RevPash;
import aros.services.rms.core.analytics.domain.OperationsReport.TableTurnover;
import aros.services.rms.core.analytics.domain.port.in.GetOperationsUseCase;
import aros.services.rms.core.analytics.infrastructure.rest.dto.OperationsReportResponse;
import aros.services.rms.core.analytics.infrastructure.rest.mapper.OperationsReportMapper;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.infraestructure.image.storage.local.LocalResourceConfig;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
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
 * Tests for {@link OperationsController}.
 *
 * <p><b>Feature:</b> Admin-only operational efficiency endpoint.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>An ADMIN request returns 200
 *   <li>An unauthenticated request returns 401
 *   <li>A WORKER request returns 403
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Boots a WebMvc slice with a JWT resource server and method security
 *   <li>Mocks the use case and mapper
 *   <li>Asserts the status for admin, anonymous and worker requests
 * </ul>
 */
@WebMvcTest(
    value = OperationsController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = LocalResourceConfig.class))
@Import(OperationsControllerWebMvcTest.TestSecurityConfig.class)
class OperationsControllerWebMvcTest {

  private static final String URL = "/api/v1/analytics/operations";
  private static final Currency COP = Currency.getInstance("COP");

  @Autowired private MockMvc mockMvc;

  @MockitoBean private GetOperationsUseCase getOperationsUseCase;
  @MockitoBean private OperationsReportMapper mapper;
  @MockitoBean private JwtDecoder jwtDecoder;

  @TestConfiguration
  @EnableMethodSecurity
  static class TestSecurityConfig {
    @Bean
    SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
      http.csrf(csrf -> csrf.disable())
          .sessionManagement(
              session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
          .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
      return http.build();
    }
  }

  @Test
  void should_allow_admin() throws Exception {
    OperationsReport report = sample();
    when(getOperationsUseCase.execute("monthly", "2026-10", "2026-10")).thenReturn(report);
    when(mapper.toResponse(report)).thenReturn(new OperationsReportResponse());

    mockMvc
        .perform(
            get(URL)
                .param("bucket", "monthly")
                .param("from", "2026-10")
                .param("to", "2026-10")
                .with(adminJwt()))
        .andExpect(status().isOk());
  }

  @Test
  void should_reject_unauthenticated() throws Exception {
    mockMvc
        .perform(
            get(URL).param("bucket", "monthly").param("from", "2026-10").param("to", "2026-10"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void should_reject_non_admin() throws Exception {
    mockMvc
        .perform(
            get(URL)
                .param("bucket", "monthly")
                .param("from", "2026-10")
                .param("to", "2026-10")
                .with(workerJwt()))
        .andExpect(status().isForbidden());
  }

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

  private static OperationsReport sample() {
    Money zero = Money.zero(COP);
    return new OperationsReport(
        new PeriodInfo("monthly", "2026-10", "2026-10", List.of("2026-10")),
        new RevPash(zero, zero, new BigDecimal("240.00")),
        new TableTurnover(BigDecimal.ZERO, List.of()),
        new LaborShare(zero, BigDecimal.ZERO),
        0,
        0,
        BigDecimal.ZERO,
        0,
        "EMPTY",
        List.of());
  }
}
