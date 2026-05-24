/* (C) 2026 */

package aros.services.rms.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.auth.application.dto.AuthResult;
import aros.services.rms.core.auth.application.dto.Credentials;
import aros.services.rms.core.auth.application.exception.InvalidCredentialsException;
import aros.services.rms.core.auth.application.exception.UserNotFoundException;
import aros.services.rms.core.auth.port.input.AccountSetupUseCase;
import aros.services.rms.core.auth.port.input.GetCurrentAuthUserInfoUseCase;
import aros.services.rms.core.auth.port.input.LoginUseCase;
import aros.services.rms.core.auth.port.input.PasswordResetUseCase;
import aros.services.rms.core.auth.port.input.RefreshTokensUseCase;
import aros.services.rms.core.auth.port.input.VerifyTwoFactorUseCase;
import aros.services.rms.infraestructure.auth.api.AuthController;
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
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import(AuthControllerTest.TestSecurityConfig.class)
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private LoginUseCase loginUseCase;
  @MockitoBean private VerifyTwoFactorUseCase verifyTwoFactorUseCase;
  @MockitoBean private RefreshTokensUseCase refreshTokensUseCase;
  @MockitoBean private GetCurrentAuthUserInfoUseCase getUserInfoUseCase;
  @MockitoBean private PasswordResetUseCase passwordResetUseCase;
  @MockitoBean private AccountSetupUseCase accountSetupUseCase;

  @org.springframework.boot.test.context.TestConfiguration
  @EnableMethodSecurity
  static class TestSecurityConfig {
    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
      http.csrf(csrf -> csrf.disable())
          .sessionManagement(
              session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .authorizeHttpRequests(
              auth ->
                  auth.requestMatchers(
                          "/api/auth/login",
                          "/api/auth/forgot-password",
                          "/api/auth/resend-password",
                          "/api/auth/reset-password",
                          "/api/auth/setup-password",
                          "/api/auth/setup-account/validate")
                      .permitAll()
                      .anyRequest()
                      .authenticated())
          .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
      return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
      return token -> null;
    }
  }

  private static final String VALID_LOGIN_BODY =
      """
      {
        "username": "test@example.com",
        "password": "validPass",
        "deviceHash": "device-123"
      }
      """;

  private static final String LOGIN_URL = "/api/auth/login";

  // ---------------------------------------------------------------------------
  // CT-01: shouldReturn200WithTokens_whenLoginSucceeds
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200WithTokens_whenLoginSucceeds() throws Exception {
    AuthResult.Success result =
        new AuthResult.Success("test@example.com", "access-token-123", "refresh-token-456");
    when(loginUseCase.authenticate(any(Credentials.class))).thenReturn(result);

    mockMvc
        .perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(VALID_LOGIN_BODY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.type").value("SUCCESS"))
        .andExpect(jsonPath("$.username").value("test@example.com"))
        .andExpect(jsonPath("$.accessToken").value("access-token-123"))
        .andExpect(jsonPath("$.refreshToken").value("refresh-token-456"));
  }

  // ---------------------------------------------------------------------------
  // CT-02: shouldReturn200WithTfa_whenLoginRequires2fa
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200WithTfa_whenLoginRequires2fa() throws Exception {
    AuthResult.RequiresTfa result = new AuthResult.RequiresTfa("test@example.com", "tfa-token-789");
    when(loginUseCase.authenticate(any(Credentials.class))).thenReturn(result);

    mockMvc
        .perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(VALID_LOGIN_BODY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.type").value("TFA_REQUIRED"))
        .andExpect(jsonPath("$.username").value("test@example.com"))
        .andExpect(jsonPath("$.accessToken").value("tfa-token-789"))
        .andExpect(jsonPath("$.refreshToken").isEmpty());
  }

  // ---------------------------------------------------------------------------
  // CT-03: shouldReturn401_whenInvalidCredentials
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn401_whenInvalidCredentials() throws Exception {
    when(loginUseCase.authenticate(any(Credentials.class)))
        .thenThrow(new InvalidCredentialsException());

    mockMvc
        .perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(VALID_LOGIN_BODY))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.message").isNotEmpty());
  }

  // ---------------------------------------------------------------------------
  // CT-04: shouldReturn400_whenEmailInvalid
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn400_whenEmailInvalid() throws Exception {
    String body =
        """
        {
          "username": "not-an-email",
          "password": "validPass",
          "deviceHash": "device-123"
        }
        """;

    mockMvc
        .perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest());
  }

  // ---------------------------------------------------------------------------
  // CT-05: shouldReturn400_whenPasswordBlank
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn400_whenPasswordBlank() throws Exception {
    String body =
        """
        {
          "username": "test@example.com",
          "password": "",
          "deviceHash": "device-123"
        }
        """;

    mockMvc
        .perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest());
  }

  // ---------------------------------------------------------------------------
  // CT-06: shouldReturn400_whenBodyEmpty
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn400_whenBodyEmpty() throws Exception {
    mockMvc
        .perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest());
  }

  // ---------------------------------------------------------------------------
  // CT-07: shouldReturn500_whenMalformedJson
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn500_whenMalformedJson() throws Exception {
    mockMvc
        .perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content("{malformed json"))
        .andExpect(status().isInternalServerError());
  }

  // ---------------------------------------------------------------------------
  // CT-08: shouldReturn200_whenExtraFieldsIgnored
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn200_whenExtraFieldsIgnored() throws Exception {
    AuthResult.Success result =
        new AuthResult.Success("test@example.com", "access-token", "refresh-token");
    when(loginUseCase.authenticate(any(Credentials.class))).thenReturn(result);

    String bodyWithExtra =
        """
        {
          "username": "test@example.com",
          "password": "validPass",
          "deviceHash": "device-123",
          "extraField": "shouldBeIgnored"
        }
        """;

    mockMvc
        .perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(bodyWithExtra))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.type").value("SUCCESS"));
  }

  // ---------------------------------------------------------------------------
  // EH-03: shouldReturn400_whenUserNotFoundOnForgotPassword
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn400_whenUserNotFoundOnForgotPassword() throws Exception {
    doThrow(new UserNotFoundException("User not found"))
        .when(passwordResetUseCase)
        .requestPasswordReset(any());

    String forgotBody =
        """
        {
          "email": "unknown@example.com"
        }
        """;

    mockMvc
        .perform(
            post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(forgotBody))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.message").value("User not found"));
  }
}
