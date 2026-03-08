/* (C) 2026 */
package aros.services.rms.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  @Value("${app.jwt.public-key}")
  private String publicKeyBase64;

  @Value("${app.jwt.private-key}")
  private String privateKeyBase64;

  @Bean
  public RSAKey rsaKey() throws Exception {
    byte[] publicBytes = Base64.getDecoder().decode(publicKeyBase64);
    byte[] privateBytes = Base64.getDecoder().decode(privateKeyBase64);

    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    RSAPublicKey publicKey =
        (RSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(publicBytes));
    RSAPrivateKey privateKey =
        (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateBytes));

    return new RSAKey.Builder(publicKey).privateKey(privateKey).build();
  }

  @Bean
  public JwtDecoder jwtDecoder(RSAKey rsaKey) throws Exception {
    return NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build();
  }

  @Bean
  public JwtEncoder jwtEncoder(RSAKey rsaKey) {
    return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(rsaKey)));
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/api/auth/login", "/swagger-ui/**", "/v3/api-docs/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:3000", "*"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);
    config.setExposedHeaders(List.of("Authorization"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
