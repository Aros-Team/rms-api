/* (C) 2026 */

package aros.services.rms.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.area.application.exception.AreaNotFoundException;
import aros.services.rms.core.auth.port.input.AccountSetupUseCase;
import aros.services.rms.core.user.application.exception.InvalidPasswordException;
import aros.services.rms.core.user.application.exception.InvalidSalaryException;
import aros.services.rms.core.user.application.exception.SamePasswordException;
import aros.services.rms.core.user.application.exception.UserAlreadyExistsException;
import aros.services.rms.core.user.application.exception.UserNotFoundByEmailException;
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
import aros.services.rms.core.user.port.input.ChangePasswordUseCase;
import aros.services.rms.core.user.port.input.CreateUserUseCase;
import aros.services.rms.core.user.port.input.CreateUserUseCase.CreateUserResult;
import aros.services.rms.core.user.port.input.DeleteUserUseCase;
import aros.services.rms.core.user.port.input.GetAllUsersUseCase;
import aros.services.rms.core.user.port.input.GetSalaryHistoryUseCase;
import aros.services.rms.core.user.port.input.RetryUserEmailUseCase;
import aros.services.rms.core.user.port.input.UpdateUserUseCase;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import aros.services.rms.infraestructure.user.api.UserController;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
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

@WebMvcTest(UserController.class)
@Import(UserControllerTest.TestSecurityConfig.class)
class UserControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private CreateUserUseCase createUserUseCase;
  @MockitoBean private ChangePasswordUseCase changePasswordUseCase;
  @MockitoBean private GetAllUsersUseCase getAllUsersUseCase;
  @MockitoBean private UpdateUserUseCase updateUserUseCase;
  @MockitoBean private DeleteUserUseCase deleteUserUseCase;
  @MockitoBean private RetryUserEmailUseCase retryUserEmailUseCase;
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

  private static final String USERS_URL = "/api/v1/users";
  private static final String CHANGE_PASSWORD_URL = "/api/v1/users/me/password";

  private static final String VALID_CREATE_BODY =
      """
      {
        "document": "1234567890",
        "name": "New User",
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
        "name": "Updated User",
        "email": "updated@example.com",
        "address": "New Address",
        "phone": "5555550200",
        "areas": [1]
      }
      """;

  private static final String VALID_CHANGE_PASSWORD_BODY =
      """
      {
        "currentPassword": "CurrentPass1!",
        "newPassword": "NewPass123!"
      }
      """;

  private User createUser(Long id, String name, String email) {
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

  private static final String SALARY_HISTORY_URL = USERS_URL + "/{id}/salary-history";

  // ---------------------------------------------------------------------------
  // CT-01: shouldReturn201_whenCreatingUser
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn201_whenCreatingUser() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createJwt("access", "ADMIN", null));

    User user = createUser(1L, "New User", "new@example.com");
    when(createUserUseCase.create(any(CreateUserInfo.class)))
        .thenReturn(new CreateUserResult(user, null));

    mockMvc
        .perform(
            post(USERS_URL)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_CREATE_BODY))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.document").value("1234567890"))
        .andExpect(jsonPath("$.name").value("New User"))
        .andExpect(jsonPath("$.email").value("new@example.com"));
  }

  // ---------------------------------------------------------------------------
  // CT-02: shouldReturn400_whenDocumentInvalid
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn400_whenDocumentInvalid() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createJwt("access", "ADMIN", null));

    String body =
        """
        {
          "document": "abc",
          "name": "New User",
          "email": "new@example.com",
          "address": "Address",
          "phone": "5555550100",
          "areas": [1]
        }
        """;

    mockMvc
        .perform(
            post(USERS_URL)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest());
  }

  // ---------------------------------------------------------------------------
  // CT-03: shouldReturn400_whenEmailInvalid
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn400_whenEmailInvalid() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createJwt("access", "ADMIN", null));

    String body =
        """
        {
          "document": "1234567890",
          "name": "New User",
          "email": "invalido",
          "address": "Address",
          "phone": "555-0100",
          "areas": [1]
        }
        """;

    mockMvc
        .perform(
            post(USERS_URL)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest());
  }

  // ---------------------------------------------------------------------------
  // CT-04: shouldReturn400_whenPhoneInvalid
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn400_whenPhoneInvalid() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createJwt("access", "ADMIN", null));

    String body =
        """
        {
          "document": "1234567890",
          "name": "New User",
          "email": "new@example.com",
          "address": "Address",
          "phone": "123",
          "areas": [1]
        }
        """;

    mockMvc
        .perform(
            post(USERS_URL)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest());
  }

  // ---------------------------------------------------------------------------
  // CT-05: shouldReturn400_whenNameContainsNumbers
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn400_whenNameContainsNumbers() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createJwt("access", "ADMIN", null));

    String body =
        """
        {
          "document": "1234567890",
          "name": "User123",
          "email": "new@example.com",
          "address": "Address",
          "phone": "555-0100",
          "areas": [1]
        }
        """;

    mockMvc
        .perform(
            post(USERS_URL)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest());
  }

  // ---------------------------------------------------------------------------
  // CT-06: shouldReturn400_whenBodyEmpty
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn400_whenBodyEmpty() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createJwt("access", "ADMIN", null));

    mockMvc
        .perform(
            post(USERS_URL)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest());
  }

  // ---------------------------------------------------------------------------
  // CT-07: shouldReturn400_whenAreasNull
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn400_whenAreasNull() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createJwt("access", "ADMIN", null));

    String body =
        """
        {
          "document": "1234567890",
          "name": "New User",
          "email": "new@example.com",
          "address": "Address",
          "phone": "555-0100",
          "areas": null
        }
        """;

    mockMvc
        .perform(
            post(USERS_URL)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest());
  }

  // ---------------------------------------------------------------------------
  // CT-08: shouldReturn200_whenUpdatingUser
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_whenUpdatingUser() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createJwt("access", "ADMIN", null));

    User user = createUser(1L, "Updated User", "updated@example.com");
    when(updateUserUseCase.update(anyLong(), any(UpdateUserInfo.class))).thenReturn(user);

    mockMvc
        .perform(
            put(USERS_URL + "/1")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_UPDATE_BODY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.name").value("Updated User"));
  }

  // ---------------------------------------------------------------------------
  // CT-09: shouldReturn404_whenUpdatingNonExistentUser
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn404_whenUpdatingNonExistentUser() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createJwt("access", "ADMIN", null));

    when(updateUserUseCase.update(anyLong(), any(UpdateUserInfo.class)))
        .thenThrow(new UserNotFoundException("User not found with id: 9999"));

    mockMvc
        .perform(
            put(USERS_URL + "/9999")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_UPDATE_BODY))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.message").value("User not found with id: 9999"));
  }

  // ---------------------------------------------------------------------------
  // CT-10: shouldReturn200_whenChangingPassword
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_whenChangingPassword() throws Exception {
    when(jwtDecoder.decode(anyString()))
        .thenReturn(createJwt("access", "WORKER", "user@example.com"));

    mockMvc
        .perform(
            put(CHANGE_PASSWORD_URL)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_CHANGE_PASSWORD_BODY))
        .andExpect(status().isOk());
  }

  // ---------------------------------------------------------------------------
  // CT-11: shouldReturn400_whenNewPasswordWeak
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn400_whenNewPasswordWeak() throws Exception {
    when(jwtDecoder.decode(anyString()))
        .thenReturn(createJwt("access", "WORKER", "user@example.com"));

    String body =
        """
        {
          "currentPassword": "CurrentPass1!",
          "newPassword": "123"
        }
        """;

    mockMvc
        .perform(
            put(CHANGE_PASSWORD_URL)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest());
  }

  // ---------------------------------------------------------------------------
  // CT-12: shouldReturn400_whenCurrentPasswordBlank
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn400_whenCurrentPasswordBlank() throws Exception {
    when(jwtDecoder.decode(anyString()))
        .thenReturn(createJwt("access", "WORKER", "user@example.com"));

    String body =
        """
        {
          "currentPassword": "",
          "newPassword": "NewPass123!"
        }
        """;

    mockMvc
        .perform(
            put(CHANGE_PASSWORD_URL)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest());
  }

  // ---------------------------------------------------------------------------
  // EH-01: shouldReturn409_whenUserAlreadyExists
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn409_whenUserAlreadyExists() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createJwt("access", "ADMIN", null));

    when(createUserUseCase.create(any(CreateUserInfo.class)))
        .thenThrow(
            new UserAlreadyExistsException(
                "El Documento o Correo ya han sido utilizados por otro usuario."));

    mockMvc
        .perform(
            post(USERS_URL)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_CREATE_BODY))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(409))
        .andExpect(jsonPath("$.message").isNotEmpty());
  }

  // ---------------------------------------------------------------------------
  // EH-02: shouldReturn422_whenAreaNotFound
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn422_whenAreaNotFound() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createJwt("access", "ADMIN", null));

    when(createUserUseCase.create(any(CreateUserInfo.class)))
        .thenThrow(
            new AreaNotFoundException("No se pudo encontrar alguna de areas referenciadas."));

    mockMvc
        .perform(
            post(USERS_URL)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_CREATE_BODY))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.message").isNotEmpty());
  }

  // ---------------------------------------------------------------------------
  // EH-04: shouldReturn400_whenEmailNotFound
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn400_whenEmailNotFound() throws Exception {
    when(jwtDecoder.decode(anyString()))
        .thenReturn(createJwt("access", "WORKER", "unknown@example.com"));

    doThrow(new UserNotFoundByEmailException())
        .when(changePasswordUseCase)
        .changePassword(anyString(), anyString(), anyString());

    mockMvc
        .perform(
            put(CHANGE_PASSWORD_URL)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_CHANGE_PASSWORD_BODY))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.message").isNotEmpty());
  }

  // ---------------------------------------------------------------------------
  // EH-05: shouldReturn400_whenInvalidPassword
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn400_whenInvalidPassword() throws Exception {
    when(jwtDecoder.decode(anyString()))
        .thenReturn(createJwt("access", "WORKER", "user@example.com"));

    doThrow(new InvalidPasswordException("La contraseña actual no es correcta"))
        .when(changePasswordUseCase)
        .changePassword(anyString(), anyString(), anyString());

    mockMvc
        .perform(
            put(CHANGE_PASSWORD_URL)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_CHANGE_PASSWORD_BODY))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.message").isNotEmpty());
  }

  // ---------------------------------------------------------------------------
  // EH-06: shouldReturn400_whenSamePassword
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn400_whenSamePassword() throws Exception {
    when(jwtDecoder.decode(anyString()))
        .thenReturn(createJwt("access", "WORKER", "user@example.com"));

    doThrow(new SamePasswordException())
        .when(changePasswordUseCase)
        .changePassword(anyString(), anyString(), anyString());

    mockMvc
        .perform(
            put(CHANGE_PASSWORD_URL)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_CHANGE_PASSWORD_BODY))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.message").isNotEmpty());
  }

  // ---------------------------------------------------------------------------
  // CT-S01: shouldReturn201_whenCreatingUserWithSalary
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn201_whenCreatingUserWithSalary() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createJwt("access", "ADMIN", null));

    User user = createUser(1L, "New User", "new@example.com");
    user.setSalary(Salary.of(new BigDecimal("2500000")));
    when(createUserUseCase.create(any(CreateUserInfo.class)))
        .thenReturn(new CreateUserResult(user, null));

    String body =
        """
        {
          "document": "1234567890",
          "name": "New User",
          "email": "new@example.com",
          "address": "Address",
          "phone": "5555550100",
          "areas": [1, 2],
          "salary": 2500000
        }
        """;

    mockMvc
        .perform(
            post(USERS_URL)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1L));
  }

  // ---------------------------------------------------------------------------
  // CT-S02: shouldReturn201_whenCreatingUserWithoutSalary
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn201_whenCreatingUserWithoutSalary() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createJwt("access", "ADMIN", null));

    User user = createUser(1L, "New User", "new@example.com");
    when(createUserUseCase.create(any(CreateUserInfo.class)))
        .thenReturn(new CreateUserResult(user, null));

    mockMvc
        .perform(
            post(USERS_URL)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_CREATE_BODY))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.salary").doesNotExist());
  }

  // ---------------------------------------------------------------------------
  // CT-S03: shouldReturn400_whenSalaryIsZero
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn400_whenSalaryIsZero() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createJwt("access", "ADMIN", null));

    String body =
        """
        {
          "document": "1234567890",
          "name": "New User",
          "email": "new@example.com",
          "address": "Address",
          "phone": "5555550100",
          "areas": [1, 2],
          "salary": 0
        }
        """;

    mockMvc
        .perform(
            post(USERS_URL)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest());
  }

  // ---------------------------------------------------------------------------
  // CT-S04: shouldReturn200_whenUpdatingUserWithSalaryChange
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_whenUpdatingUserWithSalaryChange() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createJwt("access", "ADMIN", null));

    User user = createUser(1L, "Updated User", "updated@example.com");
    user.setSalary(Salary.of(new BigDecimal("3000000")));
    when(updateUserUseCase.update(anyLong(), any(UpdateUserInfo.class))).thenReturn(user);

    String body =
        """
        {
          "document": "9876543210",
          "name": "Updated User",
          "email": "updated@example.com",
          "address": "New Address",
          "phone": "5555550200",
          "areas": [1],
          "salary": 3000000,
          "reason": "Aumento anual",
          "observations": "Periodo 2026"
        }
        """;

    mockMvc
        .perform(
            put(USERS_URL + "/1")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.salary").value(3000000));
  }

  // ---------------------------------------------------------------------------
  // CT-S05: shouldReturn400_whenSalaryChangedWithoutReason
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn400_whenSalaryChangedWithoutReason() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createJwt("access", "ADMIN", null));

    when(updateUserUseCase.update(anyLong(), any(UpdateUserInfo.class)))
        .thenThrow(
            new InvalidSalaryException("La razón es obligatoria cuando se cambia el salario"));

    String body =
        """
        {
          "document": "9876543210",
          "name": "Updated User",
          "email": "updated@example.com",
          "address": "New Address",
          "phone": "5555550200",
          "areas": [1],
          "salary": 3000000,
          "reason": null
        }
        """;

    mockMvc
        .perform(
            put(USERS_URL + "/1")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.message").value("La razón es obligatoria cuando se cambia el salario"));
  }

  // ---------------------------------------------------------------------------
  // CT-S06: shouldReturn200_whenUpdatingUserWithSameSalary
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_whenUpdatingUserWithSameSalary() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createJwt("access", "ADMIN", null));

    User user = createUser(1L, "Updated User", "updated@example.com");
    user.setSalary(Salary.of(new BigDecimal("2500000")));
    when(updateUserUseCase.update(anyLong(), any(UpdateUserInfo.class))).thenReturn(user);

    String body =
        """
        {
          "document": "9876543210",
          "name": "Updated User",
          "email": "updated@example.com",
          "address": "New Address",
          "phone": "5555550200",
          "areas": [1],
          "salary": 2500000
        }
        """;

    mockMvc
        .perform(
            put(USERS_URL + "/1")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isOk());
  }

  // ---------------------------------------------------------------------------
  // CT-S07: shouldReturn200_whenGettingSalaryHistory
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
        .andExpect(jsonPath("$[0].reason").value("Aumento"))
        .andExpect(jsonPath("$[1].oldSalary").doesNotExist())
        .andExpect(jsonPath("$[1].newSalary").value(2500000))
        .andExpect(jsonPath("$[1].reason").value("CREACION"))
        .andExpect(jsonPath("$[1].observations").value("Salario inicial"));
  }

  // ---------------------------------------------------------------------------
  // CT-S08: shouldReturn404_whenGettingSalaryHistoryForNonExistentUser
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn404_whenGettingSalaryHistoryForNonExistentUser() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createJwt("access", "ADMIN", null));

    when(getSalaryHistoryUseCase.getSalaryHistory(9999L))
        .thenThrow(new UserNotFoundException("User not found with id: 9999"));

    mockMvc
        .perform(get(SALARY_HISTORY_URL, 9999L).header("Authorization", "Bearer token"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.message").value("User not found with id: 9999"));
  }

  // ---------------------------------------------------------------------------
  // CT-S09: shouldReturn200WithEmptyList_whenUserHasNoSalaryHistory
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200WithEmptyList_whenUserHasNoSalaryHistory() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createJwt("access", "ADMIN", null));

    when(getSalaryHistoryUseCase.getSalaryHistory(1L)).thenReturn(List.of());

    mockMvc
        .perform(get(SALARY_HISTORY_URL, 1L).header("Authorization", "Bearer token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());
  }

  // ---------------------------------------------------------------------------
  // EH-S01: shouldReturn400_whenInvalidSalary
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn400_whenInvalidSalary() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createJwt("access", "ADMIN", null));

    when(createUserUseCase.create(any(CreateUserInfo.class)))
        .thenThrow(new InvalidSalaryException("El salario debe ser un valor positivo"));

    String body =
        """
        {
          "document": "1234567890",
          "name": "New User",
          "email": "new@example.com",
          "address": "Address",
          "phone": "5555550100",
          "areas": [1, 2],
          "salary": -1000
        }
        """;

    mockMvc
        .perform(
            post(USERS_URL)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.message").isNotEmpty());
  }

  // ---------------------------------------------------------------------------
  // EH-S02: shouldReturn400_whenSalaryReasonMissing
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn400_whenSalaryReasonMissing() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createJwt("access", "ADMIN", null));

    when(updateUserUseCase.update(anyLong(), any(UpdateUserInfo.class)))
        .thenThrow(
            new InvalidSalaryException("La razón es obligatoria cuando se cambia el salario"));

    String body =
        """
        {
          "document": "9876543210",
          "name": "Updated User",
          "email": "updated@example.com",
          "address": "New Address",
          "phone": "5555550200",
          "areas": [1],
          "salary": 3000000
        }
        """;

    mockMvc
        .perform(
            put(USERS_URL + "/1")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.message").isNotEmpty());
  }
}
