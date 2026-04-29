/* (C) 2026 */

package aros.services.rms.config;

import aros.services.rms.infraestructure.common.config.JwtConfigValidator;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Spring Security configuration for the application.
 *
 * <p>Configures HTTP security, CORS, JWT authentication, and public endpoint access based on
 * environment. In production, requires valid JWT tokens for most endpoints. In development, allows
 * open access for testing.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private static final String PRODUCTION = "production";

  private final JwtConfigValidator jwtConfigValidator;

  private final CorsConfigurationSource corsConfigurationSource;

  @Value("${app.env:development}")
  private String appEnv;

  /**
   * Constructs the security configuration with JWT validator and CORS configuration source.
   *
   * @param jwtConfigValidator JWT configuration validator
   * @param corsConfigurationSource CORS configuration source
   */
  public SecurityConfig(
      JwtConfigValidator jwtConfigValidator, CorsConfigurationSource corsConfigurationSource) {
    this.jwtConfigValidator = jwtConfigValidator;
    this.corsConfigurationSource = corsConfigurationSource;
  }

  /**
   * Creates the RSA key pair from PEM-encoded public and private keys.
   *
   * @return RSAKey instance or null if not configured
   * @throws Exception if key parsing fails
   */
  @Bean
  public RSAKey rsaKey() throws Exception {
    if (!jwtConfigValidator.isConfigured()) {
      jwtConfigValidator.validate();
      return null;
    }

    String publicKeyPem = jwtConfigValidator.getPublicKey();
    String privateKeyPem = jwtConfigValidator.getPrivateKey();

    Converter<InputStream, RSAPublicKey> publicKeyConverter = RsaKeyConverters.x509();
    Converter<InputStream, RSAPrivateKey> privateKeyConverter = RsaKeyConverters.pkcs8();

    RSAPublicKey publicKey =
        publicKeyConverter.convert(
            new ByteArrayInputStream(publicKeyPem.getBytes(StandardCharsets.UTF_8)));
    RSAPrivateKey privateKey =
        privateKeyConverter.convert(
            new ByteArrayInputStream(privateKeyPem.getBytes(StandardCharsets.UTF_8)));

    return new RSAKey.Builder(publicKey).privateKey(privateKey).build();
  }

  /**
   * Creates the JWT decoder for token validation.
   *
   * @param rsaKey RSA key for signature verification
   * @return JwtDecoder instance or null if key is null
   * @throws Exception if decoder creation fails
   */
  @Bean
  public JwtDecoder jwtDecoder(@Nullable RSAKey rsaKey) throws Exception {
    if (rsaKey == null) {
      return null;
    }
    return NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build();
  }

  /**
   * Creates the JWT encoder for token generation.
   *
   * @param rsaKey RSA key for signing
   * @return JwtEncoder instance or null if key is null
   */
  @Bean
  public JwtEncoder jwtEncoder(@Nullable RSAKey rsaKey) {
    if (rsaKey == null) {
      return null;
    }
    return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(rsaKey)));
  }

  /**
   * Configures and builds the security filter chain with HTTP security settings.
   *
   * @param http HttpSecurity to configure
   * @return configured SecurityFilterChain
   * @throws Exception if configuration fails
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    if (jwtConfigValidator.isConfigured()) {
      http.authorizeHttpRequests(
              auth ->
                  auth.requestMatchers(
                          "/api/auth/login",
                          "/api/auth/forgot-password",
                          "/api/auth/resend-password",
                          "/api/auth/reset-password",
                          "/api/auth/setup-password",
                          "/api/auth/setup-account/validate",
                          "/ws/**",
                          "/ws-native/**",
                          "/swagger-ui/**",
                          "/swagger-ui.html",
                          "/v3/api-docs/**",
                          "/actuator/health/**",
                          "/actuator/health",
                          "/health/**",
                          "/health",
                          "/metrics/**")
                      .permitAll()
                      .anyRequest()
                      .authenticated())
          .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
    } else {
      http.authorizeHttpRequests(
          auth ->
              auth.requestMatchers(
                      "/api/auth/login",
                      "/api/auth/forgot-password",
                      "/api/auth/resend-password",
                      "/api/auth/reset-password",
                      "/api/auth/setup-password",
                      "/api/auth/setup-account/validate",
                      "/ws/**",
                      "/ws-native/**",
                      "/swagger-ui/**",
                      "/swagger-ui.html",
                      "/v3/api-docs/**",
                      "/health/**",
                      "/health",
                      "/metrics/**")
                  .permitAll()
                  .anyRequest()
                  .permitAll());
    }

    return http.build();
  }
}
