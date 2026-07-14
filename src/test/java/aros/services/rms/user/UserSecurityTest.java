/* (C) 2026 */

package aros.services.rms.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.auth.port.input.AccountSetupUseCase;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserEmail;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.core.user.domain.UserRole;
import aros.services.rms.core.user.domain.UserStatus;
import aros.services.rms.core.user.port.dto.CreateUserInfo;
import aros.services.rms.core.user.port.dto.UpdateUserInfo;
import aros.services.rms.core.user.port.input.ChangePasswordUseCase;
import aros.services.rms.core.user.port.input.CreateUserUseCase;
import aros.services.rms.core.user.port.input.CreateUserUseCase.CreateUserResult;
import aros.services.rms.core.user.port.input.DeleteUserUseCase;
import aros.services.rms.core.user.port.input.GetAllWorkersUseCase;
import aros.services.rms.core.user.port.input.GetSalaryHistoryUseCase;
import aros.services.rms.core.user.port.input.UpdateUserUseCase;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import aros.services.rms.infraestructure.image.storage.local.LocalResourceConfig;
import aros.services.rms.infraestructure.user.api.WorkerController;
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
@Import(UserSecurityTest.TestSecurityConfig.class)
class UserSecurityTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private CreateUserUseCase createUserUseCase;
  @MockitoBean private ChangePasswordUseCase changePasswordUseCase;
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

  private static Jwt createJwt(String tokenType, String role) {
    Map<String, Object> claims = Map.of("type", tokenType, "role", role, "sub", "test@example.com");
    Map<String, Object> headers = Map.of("alg", "RS256");
    return new Jwt("token", Instant.now(), Instant.now().plusSeconds(3600), headers, claims);
  }

  private static final String WORKERS_URL = "/api/v1/workers";
  private static final String SALARY_HISTORY_URL = "/api/v1/workers/{id}/salary-history";

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

  private User createWorker(Long id) {
    return new User(
        UserId.of(id),
        "1234567890",
        "Worker",
        new UserEmail("worker@example.com"),
        "encoded",
        "Address",
        "555-0100",
        UserRole.WORKER,
        UserStatus.ACTIVE,
        List.of());
  }

  // ---------------------------------------------------------------------------
  // SG-01: shouldReturn401_whenNoTokenOnCreate
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn401_whenNoTokenOnCreate() throws Exception {
    mockMvc
        .perform(
            post(WORKERS_URL).contentType(MediaType.APPLICATION_JSON).content(VALID_CREATE_BODY))
        .andExpect(status().isUnauthorized());
  }

  // ---------------------------------------------------------------------------
  // SG-02: shouldReturn403_whenWorkerCreatesWorker
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn403_whenWorkerCreatesWorker() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createJwt("access", "WORKER"));

    mockMvc
        .perform(
            post(WORKERS_URL)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_CREATE_BODY))
        .andExpect(status().isForbidden());
  }

  // ---------------------------------------------------------------------------
  // SG-03: shouldReturn201_whenAdminCreatesWorker
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn201_whenAdminCreatesWorker() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createJwt("access", "ADMIN"));

    User worker = createWorker(1L);
    when(createUserUseCase.create(any(CreateUserInfo.class)))
        .thenReturn(new CreateUserResult(worker, null));

    mockMvc
        .perform(
            post(WORKERS_URL)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_CREATE_BODY))
        .andExpect(status().isCreated());
  }

  // ---------------------------------------------------------------------------
  // SG-04: shouldReturn403_whenWorkerUpdatesWorker
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn403_whenWorkerUpdatesWorker() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createJwt("access", "WORKER"));

    mockMvc
        .perform(
            put(WORKERS_URL + "/1")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_UPDATE_BODY))
        .andExpect(status().isForbidden());
  }

  // ---------------------------------------------------------------------------
  // SG-05: shouldReturn200_whenAdminUpdatesWorker
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_whenAdminUpdatesWorker() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createJwt("access", "ADMIN"));

    User worker = createWorker(1L);
    when(updateUserUseCase.update(anyLong(), any(UpdateUserInfo.class))).thenReturn(worker);

    mockMvc
        .perform(
            put(WORKERS_URL + "/1")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_UPDATE_BODY))
        .andExpect(status().isOk());
  }

  // ---------------------------------------------------------------------------
  // SG-S01: shouldReturn200_whenAdminGetsSalaryHistory
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_whenAdminGetsSalaryHistory() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createJwt("access", "ADMIN"));
    when(getSalaryHistoryUseCase.getSalaryHistory(anyLong())).thenReturn(List.of());

    mockMvc
        .perform(get(SALARY_HISTORY_URL, 1L).header("Authorization", "Bearer token"))
        .andExpect(status().isOk());
  }

  // ---------------------------------------------------------------------------
  // SG-S02: shouldReturn403_whenWorkerGetsSalaryHistory
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn403_whenWorkerGetsSalaryHistory() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createJwt("access", "WORKER"));

    mockMvc
        .perform(get(SALARY_HISTORY_URL, 1L).header("Authorization", "Bearer token"))
        .andExpect(status().isForbidden());
  }

  // ---------------------------------------------------------------------------
  // SG-S03: shouldReturn401_whenNoTokenOnGetSalaryHistory
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn401_whenNoTokenOnGetSalaryHistory() throws Exception {
    mockMvc.perform(get(SALARY_HISTORY_URL, 1L)).andExpect(status().isUnauthorized());
  }
}
