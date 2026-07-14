/* (C) 2026 */

package aros.services.rms.user;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.user.application.exception.InvalidPasswordException;
import aros.services.rms.core.user.application.exception.SamePasswordException;
import aros.services.rms.core.user.application.exception.UserNotFoundByEmailException;
import aros.services.rms.core.user.port.input.ChangePasswordUseCase;
import aros.services.rms.infraestructure.image.storage.local.LocalResourceConfig;
import aros.services.rms.infraestructure.user.api.UserController;
import java.time.Instant;
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
    value = UserController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = LocalResourceConfig.class))
@Import(UserControllerTest.TestSecurityConfig.class)
class UserControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private ChangePasswordUseCase changePasswordUseCase;

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

  private static final String CHANGE_PASSWORD_URL = "/api/v1/users/me/password";

  private static final String VALID_CHANGE_PASSWORD_BODY =
      """
      {
        "currentPassword": "CurrentPass1!",
        "newPassword": "NewPass123!"
      }
      """;

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
}
