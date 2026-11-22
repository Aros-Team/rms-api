/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.analytics.domain.AlertsPage;
import aros.services.rms.core.analytics.domain.exception.AlertNotFoundException;
import aros.services.rms.core.analytics.domain.port.in.GetAlertsUseCase;
import aros.services.rms.core.analytics.domain.port.in.MarkAlertReadUseCase;
import aros.services.rms.core.analytics.infrastructure.rest.dto.AlertsPageResponse;
import aros.services.rms.core.analytics.infrastructure.rest.mapper.AlertsPageMapper;
import aros.services.rms.infraestructure.image.storage.local.LocalResourceConfig;
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
 * Tests for {@link AlertsController}.
 *
 * <p><b>Feature:</b> Paged alert reads and idempotent mark-read.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>An ADMIN can list alerts and mark one read (204)
 *   <li>A missing alert returns 404 with the analytics problem type
 *   <li>Anonymous requests return 401 and WORKER requests return 403
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Boots a WebMvc slice with a JWT resource server and method security
 *   <li>Mocks the alert use cases and mapper
 *   <li>Asserts the status for admin, missing alert, anonymous and worker requests
 * </ul>
 */
@WebMvcTest(
    value = AlertsController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = LocalResourceConfig.class))
@Import(AlertsControllerWebMvcTest.TestSecurityConfig.class)
class AlertsControllerWebMvcTest {

  private static final String URL = "/api/v1/analytics/alerts";

  @Autowired private MockMvc mockMvc;

  @MockitoBean private GetAlertsUseCase getAlertsUseCase;
  @MockitoBean private MarkAlertReadUseCase markAlertReadUseCase;
  @MockitoBean private AlertsPageMapper mapper;
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
  void should_allow_admin_to_list_alerts() throws Exception {
    AlertsPage page = new AlertsPage(java.util.List.of(), new AlertsPage.PageInfo(50, 0, 0));
    when(getAlertsUseCase.execute(any(), any(), any(), any(), any(), anyInt(), anyInt()))
        .thenReturn(page);
    when(mapper.toResponse(page)).thenReturn(new AlertsPageResponse(java.util.List.of(), null));

    mockMvc.perform(get(URL).with(adminJwt())).andExpect(status().isOk());
  }

  @Test
  void should_allow_admin_to_mark_read() throws Exception {
    mockMvc.perform(patch(URL + "/17/read").with(adminJwt())).andExpect(status().isNoContent());
  }

  @Test
  void should_return_not_found_when_alert_is_missing() throws Exception {
    doThrow(new AlertNotFoundException(99L)).when(markAlertReadUseCase).markRead(99L);

    mockMvc.perform(patch(URL + "/99/read").with(adminJwt())).andExpect(status().isNotFound());
  }

  @Test
  void should_reject_unauthenticated() throws Exception {
    mockMvc.perform(get(URL)).andExpect(status().isUnauthorized());
  }

  @Test
  void should_reject_non_admin() throws Exception {
    mockMvc.perform(get(URL).with(workerJwt())).andExpect(status().isForbidden());
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
}
