/* (C) 2026 */

package aros.services.rms.infraestructure.auth.jwk;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.auth.domain.jwk.JwkAlgorithm;
import aros.services.rms.core.auth.domain.jwk.JwkKey;
import aros.services.rms.core.auth.domain.jwk.JwkKeyId;
import aros.services.rms.core.auth.domain.jwk.JwkUse;
import aros.services.rms.core.auth.domain.jwk.JwksDocument;
import aros.services.rms.core.auth.domain.jwk.exception.NoActiveSigningKeyException;
import aros.services.rms.core.auth.domain.jwk.port.input.PublishJwksUseCase;
import aros.services.rms.infraestructure.image.storage.local.LocalResourceConfig;
import com.nimbusds.jose.jwk.RSAKey;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    value = JwksController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = {JwksConfigBeans.class, LocalResourceConfig.class}))
@Import(JwksControllerWebMvcTest.TestSecurityConfig.class)
class JwksControllerWebMvcTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private PublishJwksUseCase publishJwksUseCase;

  @MockitoBean private JwksCacheProperties cacheProps;

  @org.springframework.boot.test.context.TestConfiguration
  @EnableWebSecurity
  static class TestSecurityConfig {
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      http.csrf(csrf -> csrf.disable())
          .sessionManagement(
              session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
      return http.build();
    }
  }

  @Test
  void shouldReturn200WithJwks() throws Exception {
    RSAKey key = generateRsaKey();
    JwkKey jwkKey =
        new JwkKey(new JwkKeyId(key.getKeyID()), JwkAlgorithm.RS256, JwkUse.SIGNATURE, key, true);
    when(publishJwksUseCase.publish()).thenReturn(new JwksDocument(List.of(jwkKey)));
    when(cacheProps.maxAge()).thenReturn(Duration.ofHours(1));

    mockMvc
        .perform(get("/.well-known/jwks.json").accept("application/jwk-set+json"))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Type", "application/jwk-set+json"))
        .andExpect(header().exists("Cache-Control"))
        .andExpect(header().string("X-Content-Type-Options", "nosniff"))
        .andExpect(jsonPath("$.keys[0].kty").value("RSA"))
        .andExpect(jsonPath("$.keys[0].kid").exists())
        .andExpect(jsonPath("$.keys[0].alg").value("RS256"))
        .andExpect(jsonPath("$.keys[0].use").value("sig"))
        .andExpect(jsonPath("$.keys[0].n").exists())
        .andExpect(jsonPath("$.keys[0].e").exists())
        .andExpect(jsonPath("$.keys[0].d").doesNotExist())
        .andExpect(jsonPath("$.keys[0].p").doesNotExist())
        .andExpect(jsonPath("$.keys[0].q").doesNotExist())
        .andExpect(jsonPath("$.keys[0].dp").doesNotExist())
        .andExpect(jsonPath("$.keys[0].dq").doesNotExist())
        .andExpect(jsonPath("$.keys[0].qi").doesNotExist());
  }

  @Test
  void shouldReturn503_whenNoActiveSigningKey() throws Exception {
    when(publishJwksUseCase.publish()).thenThrow(new NoActiveSigningKeyException());

    mockMvc
        .perform(get("/.well-known/jwks.json").accept("application/jwk-set+json"))
        .andExpect(status().isServiceUnavailable());
  }

  private static RSAKey generateRsaKey() {
    try {
      KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
      gen.initialize(2048);
      KeyPair kp = gen.generateKeyPair();
      JwkKeyId kid = JwkKeyIdDeriver.from((RSAPublicKey) kp.getPublic());
      return new RSAKey.Builder((RSAPublicKey) kp.getPublic())
          .privateKey(kp.getPrivate())
          .keyID(kid.value())
          .keyUse(com.nimbusds.jose.jwk.KeyUse.SIGNATURE)
          .algorithm(com.nimbusds.jose.Algorithm.parse("RS256"))
          .build();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
