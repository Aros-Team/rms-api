/* (C) 2026 */

package aros.services.rms.schedule;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.schedule.application.exception.ScheduleAlreadyExistsException;
import aros.services.rms.core.schedule.application.exception.ScheduleHasAssignmentsException;
import aros.services.rms.core.schedule.domain.DayOfWeek;
import aros.services.rms.core.schedule.domain.Schedule;
import aros.services.rms.core.schedule.domain.ScheduleId;
import aros.services.rms.core.schedule.domain.ScheduleShift;
import aros.services.rms.core.schedule.port.input.CreateScheduleUseCase;
import aros.services.rms.core.schedule.port.input.DeleteScheduleUseCase;
import aros.services.rms.core.schedule.port.input.UpdateScheduleUseCase;
import aros.services.rms.core.schedule.port.output.ScheduleRepositoryPort;
import aros.services.rms.infraestructure.image.storage.local.LocalResourceConfig;
import aros.services.rms.infraestructure.schedule.api.ScheduleController;
import java.time.Instant;
import java.time.LocalTime;
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
    value = ScheduleController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = LocalResourceConfig.class))
@Import(ScheduleControllerTest.TestSecurityConfig.class)
class ScheduleControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private CreateScheduleUseCase createScheduleUseCase;
  @MockitoBean private UpdateScheduleUseCase updateScheduleUseCase;
  @MockitoBean private DeleteScheduleUseCase deleteScheduleUseCase;
  @MockitoBean private ScheduleRepositoryPort scheduleRepository;
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

  private static final String URL = "/api/v1/schedules";
  private static final String VALID_BODY =
      """
      {
        "name": "Morning",
        "description": "Morning shift",
        "shifts": [{"dayOfWeek": "MONDAY", "startTime": "08:00", "endTime": "12:00"}]
      }
      """;

  @Test
  void shouldCreateSchedule() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createAdminJwt());
    var schedule =
        new Schedule(
            ScheduleId.of(1L),
            "Morning",
            "Morning shift",
            List.of(new ScheduleShift(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0))));
    when(createScheduleUseCase.create(any())).thenReturn(schedule);

    mockMvc
        .perform(
            post(URL)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("Morning"));
  }

  @Test
  void shouldReturn409_whenScheduleNameExists() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createAdminJwt());
    when(createScheduleUseCase.create(any()))
        .thenThrow(
            new ScheduleAlreadyExistsException("Schedule already exists with name: Morning"));

    mockMvc
        .perform(
            post(URL)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
        .andExpect(status().isConflict());
  }

  @Test
  void shouldGetAllSchedules() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createAdminJwt());
    var schedule =
        new Schedule(
            ScheduleId.of(1L),
            "Morning",
            "Morning shift",
            List.of(new ScheduleShift(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0))));
    when(scheduleRepository.findAll()).thenReturn(List.of(schedule));

    mockMvc
        .perform(get(URL).header("Authorization", "Bearer token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name").value("Morning"));
  }

  @Test
  void shouldGetScheduleById() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createAdminJwt());
    var schedule =
        new Schedule(
            ScheduleId.of(1L),
            "Morning",
            "Morning shift",
            List.of(new ScheduleShift(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0))));
    when(scheduleRepository.findById(ScheduleId.of(1L))).thenReturn(Optional.of(schedule));

    mockMvc
        .perform(get(URL + "/1").header("Authorization", "Bearer token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1));
  }

  @Test
  void shouldReturn404_whenScheduleNotFound() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createAdminJwt());
    when(scheduleRepository.findById(ScheduleId.of(99L))).thenReturn(Optional.empty());

    mockMvc
        .perform(get(URL + "/99").header("Authorization", "Bearer token"))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldUpdateSchedule() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createAdminJwt());
    var schedule =
        new Schedule(
            ScheduleId.of(1L),
            "Evening",
            "Evening shift",
            List.of(new ScheduleShift(DayOfWeek.MONDAY, LocalTime.of(14, 0), LocalTime.of(18, 0))));
    when(updateScheduleUseCase.update(any(), any())).thenReturn(schedule);

    mockMvc
        .perform(
            put(URL + "/1")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Evening"));
  }

  @Test
  void shouldDeleteSchedule() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createAdminJwt());
    doNothing().when(deleteScheduleUseCase).delete(any());

    mockMvc
        .perform(delete(URL + "/1").header("Authorization", "Bearer token"))
        .andExpect(status().isNoContent());
  }

  @Test
  void shouldReturn409_whenDeleteScheduleWithAssignments() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createAdminJwt());
    doThrow(
            new ScheduleHasAssignmentsException(
                "Cannot delete schedule with id: 1 because it has active assignments"))
        .when(deleteScheduleUseCase)
        .delete(any());

    mockMvc
        .perform(delete(URL + "/1").header("Authorization", "Bearer token"))
        .andExpect(status().isConflict());
  }
}
