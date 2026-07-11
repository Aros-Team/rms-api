/* (C) 2026 */

package aros.services.rms.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.auth.application.dto.AuthResult;
import aros.services.rms.core.auth.application.dto.Credentials;
import aros.services.rms.core.auth.application.exception.InvalidRefreshTokenException;
import aros.services.rms.core.auth.port.input.AccountSetupUseCase;
import aros.services.rms.core.auth.port.input.GetCurrentAuthUserInfoUseCase;
import aros.services.rms.core.auth.port.input.LoginUseCase;
import aros.services.rms.core.auth.port.input.PasswordResetUseCase;
import aros.services.rms.core.auth.port.input.RefreshTokensUseCase;
import aros.services.rms.core.auth.port.input.VerifyTwoFactorUseCase;
import aros.services.rms.infraestructure.auth.api.AuthController;
import aros.services.rms.infraestructure.image.storage.local.LocalResourceConfig;
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
    value = AuthController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = LocalResourceConfig.class))
@Import(AuthSecurityTest.TestSecurityConfig.class)
class AuthSecurityTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private LoginUseCase loginUseCase;
  @MockitoBean private AccountSetupUseCase accountSetupUseCase;
  @MockitoBean private VerifyTwoFactorUseCase verifyTwoFactorUseCase;
  @MockitoBean private RefreshTokensUseCase refreshTokensUseCase;
  @MockitoBean private GetCurrentAuthUserInfoUseCase getUserInfoUseCase;
  @MockitoBean private PasswordResetUseCase passwordResetUseCase;

  @MockitoBean private JwtDecoder jwtDecoder;

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
  }

  private static Jwt createJwt(String tokenType, String tokenValue) {
    Map<String, Object> claims = Map.of("type", tokenType, "sub", "test@example.com");
    Map<String, Object> headers = Map.of("alg", "RS256");
    return new Jwt(tokenValue, Instant.now(), Instant.now().plusSeconds(3600), headers, claims);
  }

  // ---------------------------------------------------------------------------
  // SG-01: shouldAllowPublicAccess_whenLoginWithoutAuth
  // ---------------------------------------------------------------------------

  @Test
  void shouldAllowPublicAccess_whenLoginWithoutAuth() throws Exception {
    AuthResult.Success result =
        new AuthResult.Success("test@example.com", "access-token", "refresh-token");
    when(loginUseCase.authenticate(any(Credentials.class))).thenReturn(result);

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"username": "test@example.com", "password": "pass", "deviceHash": "d1"}
                    """))
        .andExpect(status().isOk());
  }

  // ---------------------------------------------------------------------------
  // SG-02: shouldAllowPublicAccess_whenForgotPasswordWithoutAuth
  // ---------------------------------------------------------------------------

  @Test
  void shouldAllowPublicAccess_whenForgotPasswordWithoutAuth() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"email": "test@example.com"}
                    """))
        .andExpect(status().isOk());
  }

  // ---------------------------------------------------------------------------
  // SG-03: shouldReturn403_whenVerifyEndpointWithAccessToken
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn403_whenVerifyEndpointWithAccessToken() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createJwt("access", "access-token"));

    mockMvc
        .perform(
            post("/api/auth/verify")
                .header("Authorization", "Bearer access-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"code": "123456", "deviceHash": "d1"}
                    """))
        .andExpect(status().isForbidden());
  }

  // ---------------------------------------------------------------------------
  // SG-04: shouldReturn403_whenRefreshEndpointWithTfaToken
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn403_whenRefreshEndpointWithTfaToken() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createJwt("tfa", "tfa-token"));

    mockMvc
        .perform(post("/api/auth/refresh").header("Authorization", "Bearer tfa-token"))
        .andExpect(status().isForbidden());
  }

  // ---------------------------------------------------------------------------
  // SG-05: shouldReturn401_whenGetMeWithoutAuth
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn401_whenGetMeWithoutAuth() throws Exception {
    mockMvc.perform(get("/api/auth")).andExpect(status().isUnauthorized());
  }

  // ---------------------------------------------------------------------------
  // EH-02: shouldReturn401_whenRefreshTokenIsInvalid
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturn401_whenRefreshTokenIsInvalid() throws Exception {
    when(jwtDecoder.decode(anyString())).thenReturn(createJwt("refresh", "refresh-token"));
    when(refreshTokensUseCase.refresh(any()))
        .thenThrow(new InvalidRefreshTokenException("Refresh token expired or revoked"));

    mockMvc
        .perform(post("/api/auth/refresh").header("Authorization", "Bearer refresh-token"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.message").value("Refresh token expired or revoked"));
  }
}
