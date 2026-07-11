/* (C) 2026 */

package aros.services.rms.schedule;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.schedule.domain.DayOfWeek;
import aros.services.rms.core.schedule.port.input.GetWorkerShiftsUseCase;
import aros.services.rms.core.schedule.port.input.GetWorkerShiftsUseCase.ShiftDetail;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserEmail;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.core.user.domain.UserRole;
import aros.services.rms.core.user.domain.UserStatus;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import aros.services.rms.infraestructure.schedule.api.WorkerScheduleController;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

@WebMvcTest(WorkerScheduleController.class)
@Import(WorkerScheduleControllerTest.TestSecurityConfig.class)
class WorkerScheduleControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private GetWorkerShiftsUseCase getWorkerShiftsUseCase;
  @MockitoBean private UserRepositoryPort userRepositoryPort;
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

  private static Jwt createWorkerJwt() {
    return new Jwt(
        "token",
        Instant.now(),
        Instant.now().plusSeconds(3600),
        Map.of("alg", "RS256"),
        Map.of("type", "access", "role", "WORKER", "sub", "worker@test.com"));
  }

  private static final String URL = "/api/v1/workers/me/schedule";

  @Test
  void shouldReturnMySchedule() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createWorkerJwt());
    var user =
        new User(
            UserId.of(1L),
            "123",
            "Worker",
            new UserEmail("worker@test.com"),
            "encoded",
            "Addr",
            "555",
            UserRole.WORKER,
            UserStatus.ACTIVE,
            List.of());
    when(userRepositoryPort.findByEmail("worker@test.com")).thenReturn(Optional.of(user));

    var shifts =
        Map.of(
            DayOfWeek.MONDAY,
            List.of(new ShiftDetail("Morning", LocalTime.of(8, 0), LocalTime.of(12, 0))));
    when(getWorkerShiftsUseCase.getShifts(UserId.of(1L))).thenReturn(shifts);

    mockMvc
        .perform(get(URL).header("Authorization", "Bearer token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.days[0].dayOfWeek").value("MONDAY"));
  }
}
