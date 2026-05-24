/* (C) 2026 */

package aros.services.rms.schedule;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.schedule.domain.LogType;
import aros.services.rms.core.schedule.domain.TimeLog;
import aros.services.rms.core.schedule.domain.TimeLogId;
import aros.services.rms.core.schedule.port.input.GetTimeLogHistoryUseCase;
import aros.services.rms.core.schedule.port.input.GetTimeLogHistoryUseCase.TimeLogFilter;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.infraestructure.schedule.api.TimeLogController;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TimeLogController.class)
@Import(TimeLogControllerTest.TestSecurityConfig.class)
class TimeLogControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private GetTimeLogHistoryUseCase getTimeLogHistoryUseCase;
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

  private static Jwt createAdminJwt() {
    return new Jwt(
        "token",
        Instant.now(),
        Instant.now().plusSeconds(3600),
        Map.of("alg", "RS256"),
        Map.of("type", "access", "role", "ADMIN", "sub", "admin@test.com"));
  }

  private static final String URL = "/api/v1/admin/time-logs";

  @Test
  void shouldReturnTimeLogs() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createAdminJwt());
    var log = new TimeLog(TimeLogId.of(1L), UserId.of(1L), Instant.now(), LogType.IN, true, 1L);
    when(getTimeLogHistoryUseCase.getHistory(any(TimeLogFilter.class))).thenReturn(List.of(log));

    mockMvc
        .perform(get(URL).header("Authorization", "Bearer token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].type").value("IN"));
  }

  @Test
  void shouldFilterByWorkerId() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createAdminJwt());
    when(getTimeLogHistoryUseCase.getHistory(any(TimeLogFilter.class))).thenReturn(List.of());

    mockMvc
        .perform(get(URL + "?workerId=1").header("Authorization", "Bearer token"))
        .andExpect(status().isOk());
  }
}
