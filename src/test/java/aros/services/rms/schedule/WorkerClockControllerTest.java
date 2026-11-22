/* (C) 2026 */

package aros.services.rms.schedule;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.schedule.application.exception.NoOpenSessionException;
import aros.services.rms.core.schedule.application.exception.OpenSessionAlreadyExistsException;
import aros.services.rms.core.schedule.domain.LogType;
import aros.services.rms.core.schedule.domain.TimeLog;
import aros.services.rms.core.schedule.domain.TimeLogId;
import aros.services.rms.core.schedule.port.input.ClockInUseCase;
import aros.services.rms.core.schedule.port.input.ClockOutUseCase;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserEmail;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.core.user.domain.UserRole;
import aros.services.rms.core.user.domain.UserStatus;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import aros.services.rms.infraestructure.image.storage.local.LocalResourceConfig;
import aros.services.rms.infraestructure.schedule.api.WorkerClockController;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests for {@link WorkerClockController}.
 *
 * <p><b>Feature:</b> Worker clock-in/clock-out REST endpoints
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>should Clock In The Authenticated Worker
 *   <li>should Clock Out The Authenticated Worker
 *   <li>should Return Conflict when a Session Is Already Open
 *   <li>should Return Conflict when There Is No Open Session
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Uses MockMvc with a worker JWT
 *   <li>Mocks the use cases and the user lookup
 * </ul>
 */
@WebMvcTest(
    value = WorkerClockController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = LocalResourceConfig.class))
@Import(WorkerClockControllerTest.TestSecurityConfig.class)
class WorkerClockControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private ClockInUseCase clockInUseCase;
  @MockitoBean private ClockOutUseCase clockOutUseCase;
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

  private static final String BASE_URL = "/api/v1/workers/me";

  private static Jwt createWorkerJwt() {
    return new Jwt(
        "token",
        Instant.now(),
        Instant.now().plusSeconds(3600),
        Map.of("alg", "RS256"),
        Map.of("type", "access", "role", "WORKER", "sub", "worker@test.com"));
  }

  private void mockWorkerLookup() {
    var user =
        new User(
            UserId.of(7L),
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
  }

  @Test
  void shouldClockInTheAuthenticatedWorker() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createWorkerJwt());
    mockWorkerLookup();
    var entry = new TimeLog(TimeLogId.of(50L), UserId.of(7L), Instant.now(), LogType.IN, true, 10L);
    when(clockInUseCase.clockIn(UserId.of(7L))).thenReturn(entry);

    mockMvc
        .perform(post(BASE_URL + "/clock-in").header("Authorization", "Bearer token"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.id").value(50))
        .andExpect(jsonPath("$.data.workerId").value(7))
        .andExpect(jsonPath("$.data.type").value("IN"))
        .andExpect(jsonPath("$.data.withinShift").value(true));
  }

  @Test
  void shouldClockOutTheAuthenticatedWorker() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createWorkerJwt());
    mockWorkerLookup();
    var entry =
        new TimeLog(TimeLogId.of(51L), UserId.of(7L), Instant.now(), LogType.OUT, true, 10L);
    when(clockOutUseCase.clockOut(UserId.of(7L))).thenReturn(entry);

    mockMvc
        .perform(post(BASE_URL + "/clock-out").header("Authorization", "Bearer token"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.type").value("OUT"));
  }

  @Test
  void shouldReturnConflictWhenSessionIsAlreadyOpen() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createWorkerJwt());
    mockWorkerLookup();
    when(clockInUseCase.clockIn(any()))
        .thenThrow(new OpenSessionAlreadyExistsException("open session"));

    mockMvc
        .perform(post(BASE_URL + "/clock-in").header("Authorization", "Bearer token"))
        .andExpect(status().isConflict());
  }

  @Test
  void shouldReturnConflictWhenThereIsNoOpenSession() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createWorkerJwt());
    mockWorkerLookup();
    when(clockOutUseCase.clockOut(any())).thenThrow(new NoOpenSessionException("no session"));

    mockMvc
        .perform(post(BASE_URL + "/clock-out").header("Authorization", "Bearer token"))
        .andExpect(status().isConflict());
  }
}
