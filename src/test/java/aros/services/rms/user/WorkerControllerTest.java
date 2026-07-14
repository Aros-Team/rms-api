/* (C) 2026 */

package aros.services.rms.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.auth.port.input.AccountSetupUseCase;
import aros.services.rms.core.user.application.exception.UserAlreadyExistsException;
import aros.services.rms.core.user.application.exception.UserNotFoundException;
import aros.services.rms.core.user.domain.Salary;
import aros.services.rms.core.user.domain.SalaryHistoryEntry;
import aros.services.rms.core.user.domain.SalaryHistoryId;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserEmail;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.core.user.domain.UserRole;
import aros.services.rms.core.user.domain.UserStatus;
import aros.services.rms.core.user.port.dto.CreateUserInfo;
import aros.services.rms.core.user.port.dto.UpdateUserInfo;
import aros.services.rms.core.user.port.input.CreateUserUseCase;
import aros.services.rms.core.user.port.input.CreateUserUseCase.CreateUserResult;
import aros.services.rms.core.user.port.input.DeleteUserUseCase;
import aros.services.rms.core.user.port.input.GetAllWorkersUseCase;
import aros.services.rms.core.user.port.input.GetSalaryHistoryUseCase;
import aros.services.rms.core.user.port.input.UpdateUserUseCase;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import aros.services.rms.infraestructure.image.storage.local.LocalResourceConfig;
import aros.services.rms.infraestructure.user.api.WorkerController;
import java.math.BigDecimal;
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
    value = WorkerController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = LocalResourceConfig.class))
@Import(WorkerControllerTest.TestSecurityConfig.class)
class WorkerControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private CreateUserUseCase createUserUseCase;
  @MockitoBean private GetAllWorkersUseCase getAllWorkersUseCase;
  @MockitoBean private UpdateUserUseCase updateUserUseCase;
  @MockitoBean private DeleteUserUseCase deleteUserUseCase;
  @MockitoBean private AccountSetupUseCase accountSetupUseCase;
  @MockitoBean private UserRepositoryPort userRepositoryPort;
  @MockitoBean private GetSalaryHistoryUseCase getSalaryHistoryUseCase;

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

  private static Jwt createJwt(String tokenType, String role, String subject) {
    Map<String, Object> claims =
        Map.of(
            "type", tokenType, "role", role, "sub", subject != null ? subject : "admin@test.com");
    Map<String, Object> headers = Map.of("alg", "RS256");
    return new Jwt("token", Instant.now(), Instant.now().plusSeconds(3600), headers, claims);
  }

  private User createWorker(Long id, String name, String email) {
    return new User(
        UserId.of(id),
        "1234567890",
        name,
        new UserEmail(email),
        "encoded",
        "Address",
        "555-0100",
        UserRole.WORKER,
        UserStatus.ACTIVE,
        List.of());
  }

  private SalaryHistoryEntry createSalaryEntry(
      Long entryId, BigDecimal oldVal, BigDecimal newVal, String reason, String observations) {
    return new SalaryHistoryEntry(
        entryId != null ? new SalaryHistoryId(entryId) : null,
        UserId.of(1L),
        oldVal != null ? Salary.of(oldVal) : null,
        Salary.of(newVal),
        Instant.now(),
        reason,
        observations);
  }

  private static final String WORKERS_URL = "/api/v1/workers";
  private static final String SALARY_HISTORY_URL = WORKERS_URL + "/{id}/salary-history";

  private static final String VALID_CREATE_BODY =
      """
      {
        "document": "1234567890",
        "name": "New Worker",
        "email": "new@example.com",
        "address": "Address",
        "phone": "5555550100",
        "areas": [1, 2]
      }
      """;

  private static final String VALID_UPDATE_BODY =
      """
      {
        "document": "9876543210",
        "name": "Updated Worker",
        "email": "updated@example.com",
        "address": "New Address",
        "phone": "5555550200",
        "areas": [1]
      }
      """;

  // ---------------------------------------------------------------------------
  // CT-G01: shouldReturn200_whenListingWorkers
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_whenListingWorkers() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createJwt("access", "ADMIN", null));

    User worker1 = createWorker(1L, "Worker One", "worker1@example.com");
    User worker2 = createWorker(2L, "Worker Two", "worker2@example.com");
    when(getAllWorkersUseCase.getAll()).thenReturn(List.of(worker1, worker2));

    mockMvc
        .perform(get(WORKERS_URL).header("Authorization", "Bearer token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].name").value("Worker One"))
        .andExpect(jsonPath("$[1].name").value("Worker Two"));
  }

  // ---------------------------------------------------------------------------
  // CT-G02: shouldReturn200_whenNoWorkers
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_whenNoWorkers() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createJwt("access", "ADMIN", null));

    when(getAllWorkersUseCase.getAll()).thenReturn(List.of());

    mockMvc
        .perform(get(WORKERS_URL).header("Authorization", "Bearer token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());
  }

  // ---------------------------------------------------------------------------
  // CT-C01: shouldReturn201_whenCreatingWorker
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn201_whenCreatingWorker() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createJwt("access", "ADMIN", null));

    User worker = createWorker(1L, "New Worker", "new@example.com");
    when(createUserUseCase.create(any(CreateUserInfo.class)))
        .thenReturn(new CreateUserResult(worker, null));

    mockMvc
        .perform(
            post(WORKERS_URL)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_CREATE_BODY))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.name").value("New Worker"));
  }

  // ---------------------------------------------------------------------------
  // CT-C02: shouldReturn400_whenCreateBodyInvalid
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn400_whenCreateBodyInvalid() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createJwt("access", "ADMIN", null));

    mockMvc
        .perform(
            post(WORKERS_URL)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest());
  }

  // ---------------------------------------------------------------------------
  // CT-C03: shouldReturn409_whenWorkerAlreadyExists
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn409_whenWorkerAlreadyExists() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createJwt("access", "ADMIN", null));

    when(createUserUseCase.create(any(CreateUserInfo.class)))
        .thenThrow(new UserAlreadyExistsException("Worker already exists"));

    mockMvc
        .perform(
            post(WORKERS_URL)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_CREATE_BODY))
        .andExpect(status().isConflict());
  }

  // ---------------------------------------------------------------------------
  // CT-U01: shouldReturn200_whenUpdatingWorker
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_whenUpdatingWorker() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createJwt("access", "ADMIN", null));

    User worker = createWorker(1L, "Updated Worker", "updated@example.com");
    when(updateUserUseCase.update(anyLong(), any(UpdateUserInfo.class))).thenReturn(worker);

    mockMvc
        .perform(
            put(WORKERS_URL + "/1")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_UPDATE_BODY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Updated Worker"));
  }

  // ---------------------------------------------------------------------------
  // CT-U02: shouldReturn404_whenUpdatingNonExistentWorker
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn404_whenUpdatingNonExistentWorker() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createJwt("access", "ADMIN", null));

    when(updateUserUseCase.update(anyLong(), any(UpdateUserInfo.class)))
        .thenThrow(new UserNotFoundException("Worker not found with id: 9999"));

    mockMvc
        .perform(
            put(WORKERS_URL + "/9999")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_UPDATE_BODY))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404));
  }

  // ---------------------------------------------------------------------------
  // CT-D01: shouldReturn204_whenDeletingWorker
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn204_whenDeletingWorker() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createJwt("access", "ADMIN", null));

    mockMvc
        .perform(delete(WORKERS_URL + "/1").header("Authorization", "Bearer token"))
        .andExpect(status().isNoContent());
  }

  // ---------------------------------------------------------------------------
  // CT-D02: shouldReturn404_whenDeletingNonExistentWorker
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn404_whenDeletingNonExistentWorker() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createJwt("access", "ADMIN", null));

    doThrow(new UserNotFoundException("Worker not found"))
        .when(deleteUserUseCase)
        .delete(anyLong());

    mockMvc
        .perform(delete(WORKERS_URL + "/9999").header("Authorization", "Bearer token"))
        .andExpect(status().isNotFound());
  }

  // ---------------------------------------------------------------------------
  // CT-S01: shouldReturn200_whenGettingSalaryHistory
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_whenGettingSalaryHistory() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createJwt("access", "ADMIN", null));

    List<SalaryHistoryEntry> entries =
        List.of(
            createSalaryEntry(
                2L, new BigDecimal("2500000"), new BigDecimal("3000000"), "Aumento", null),
            createSalaryEntry(1L, null, new BigDecimal("2500000"), "CREACION", "Salario inicial"));

    when(getSalaryHistoryUseCase.getSalaryHistory(1L)).thenReturn(entries);

    mockMvc
        .perform(get(SALARY_HISTORY_URL, 1L).header("Authorization", "Bearer token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].oldSalary").value(2500000))
        .andExpect(jsonPath("$[0].newSalary").value(3000000))
        .andExpect(jsonPath("$[1].oldSalary").doesNotExist());
  }

  // ---------------------------------------------------------------------------
  // SEC-01: shouldReturn403_whenWorkerUser
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn403_whenWorkerUser() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createJwt("access", "WORKER", null));

    mockMvc
        .perform(get(WORKERS_URL).header("Authorization", "Bearer token"))
        .andExpect(status().isForbidden());
  }

  // ---------------------------------------------------------------------------
  // SEC-02: shouldReturn401_whenNoToken
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn401_whenNoToken() throws Exception {
    mockMvc.perform(get(WORKERS_URL)).andExpect(status().isUnauthorized());
  }
}
