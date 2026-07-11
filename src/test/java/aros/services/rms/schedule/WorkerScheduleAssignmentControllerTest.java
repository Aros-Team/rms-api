/* (C) 2026 */

package aros.services.rms.schedule;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.schedule.domain.ScheduleId;
import aros.services.rms.core.schedule.domain.WorkerScheduleAssignment;
import aros.services.rms.core.schedule.domain.WorkerScheduleAssignmentId;
import aros.services.rms.core.schedule.port.input.AssignScheduleToWorkerUseCase;
import aros.services.rms.core.schedule.port.input.RemoveScheduleFromWorkerUseCase;
import aros.services.rms.core.schedule.port.output.WorkerScheduleAssignmentRepositoryPort;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.infraestructure.image.storage.local.LocalResourceConfig;
import aros.services.rms.infraestructure.schedule.api.WorkerScheduleAssignmentController;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    value = WorkerScheduleAssignmentController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = LocalResourceConfig.class))
@Import(WorkerScheduleAssignmentControllerTest.TestSecurityConfig.class)
class WorkerScheduleAssignmentControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private AssignScheduleToWorkerUseCase assignScheduleUseCase;
  @MockitoBean private RemoveScheduleFromWorkerUseCase removeScheduleUseCase;
  @MockitoBean private WorkerScheduleAssignmentRepositoryPort assignmentRepository;
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

  private static final String URL = "/api/v1/workers/1/schedule-assignments";

  @Test
  void shouldAssignScheduleToWorker() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createAdminJwt());
    var assignment = new WorkerScheduleAssignment(UserId.of(1L), ScheduleId.of(1L));
    when(assignScheduleUseCase.assign(any())).thenReturn(assignment);

    mockMvc
        .perform(
            post(URL)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"scheduleId\": 1}"))
        .andExpect(status().isCreated());
  }

  @Test
  void shouldListAssignments() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createAdminJwt());
    var assignment =
        new WorkerScheduleAssignment(
            WorkerScheduleAssignmentId.of(10L), UserId.of(1L), ScheduleId.of(1L), Instant.now());
    when(assignmentRepository.findByWorkerId(UserId.of(1L))).thenReturn(List.of(assignment));

    mockMvc
        .perform(get(URL).header("Authorization", "Bearer token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0]").value(1));
  }

  @Test
  void shouldRemoveAssignment() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createAdminJwt());
    doNothing().when(removeScheduleUseCase).remove(any());

    mockMvc
        .perform(delete(URL + "/10").header("Authorization", "Bearer token"))
        .andExpect(status().isNoContent());
  }
}
