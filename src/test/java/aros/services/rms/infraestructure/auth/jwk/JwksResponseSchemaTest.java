/* (C) 2026 */

package aros.services.rms.infraestructure.auth.jwk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.auth.domain.jwk.JwkAlgorithm;
import aros.services.rms.core.auth.domain.jwk.JwkKey;
import aros.services.rms.core.auth.domain.jwk.JwkKeyId;
import aros.services.rms.core.auth.domain.jwk.JwkUse;
import aros.services.rms.core.auth.domain.jwk.JwksDocument;
import aros.services.rms.core.auth.domain.jwk.port.input.PublishJwksUseCase;
import aros.services.rms.infraestructure.image.storage.local.LocalResourceConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.RSAKey;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
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
@Import(JwksResponseSchemaTest.TestSecurityConfig.class)
class JwksResponseSchemaTest {

  @Autowired private MockMvc mockMvc;

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
  }

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
  void shouldMatchRfc7517Shape() throws Exception {
    RSAKey key = generateRsaKey();
    JwkKey jwkKey =
        new JwkKey(new JwkKeyId(key.getKeyID()), JwkAlgorithm.RS256, JwkUse.SIGNATURE, key, true);
    when(publishJwksUseCase.publish()).thenReturn(new JwksDocument(List.of(jwkKey)));
    when(cacheProps.maxAge()).thenReturn(Duration.ofHours(1));

    String json =
        mockMvc
            .perform(get("/.well-known/jwks.json").accept("application/jwk-set+json"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    JsonNode root = objectMapper.readTree(json);
    assertThat(root.has("keys")).isTrue();
    assertThat(root.get("keys").isArray()).isTrue();
    assertThat(root.get("keys").size()).isEqualTo(1);

    JsonNode keyNode = root.get("keys").get(0);
    assertThat(keyNode.has("kty")).isTrue();
    assertThat(keyNode.get("kty").asText()).isEqualTo("RSA");
    assertThat(keyNode.has("kid")).isTrue();
    assertThat(keyNode.get("kid").asText()).startsWith("k-");
    assertThat(keyNode.has("alg")).isTrue();
    assertThat(keyNode.get("alg").asText()).isEqualTo("RS256");
    assertThat(keyNode.has("use")).isTrue();
    assertThat(keyNode.get("use").asText()).isEqualTo("sig");
    assertThat(keyNode.has("n")).isTrue();
    assertThat(keyNode.get("n").asText()).isNotEmpty();
    assertThat(keyNode.has("e")).isTrue();
    assertThat(keyNode.get("e").asText()).isNotEmpty();

    assertThat(keyNode.has("d")).isFalse();
    assertThat(keyNode.has("p")).isFalse();
    assertThat(keyNode.has("q")).isFalse();
    assertThat(keyNode.has("dp")).isFalse();
    assertThat(keyNode.has("dq")).isFalse();
    assertThat(keyNode.has("qi")).isFalse();
  }

  @Test
  void shouldRejectUnexpectedFields() throws Exception {
    RSAKey key = generateRsaKey();
    JwkKey jwkKey =
        new JwkKey(new JwkKeyId(key.getKeyID()), JwkAlgorithm.RS256, JwkUse.SIGNATURE, key, true);
    when(publishJwksUseCase.publish()).thenReturn(new JwksDocument(List.of(jwkKey)));
    when(cacheProps.maxAge()).thenReturn(Duration.ofHours(1));

    String json =
        mockMvc
            .perform(get("/.well-known/jwks.json").accept("application/jwk-set+json"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    JsonNode root = objectMapper.readTree(json);
    JsonNode keyNode = root.get("keys").get(0);

    List<String> actualFields = new ArrayList<>();
    keyNode.fieldNames().forEachRemaining(actualFields::add);
    assertThat(actualFields)
        .allMatch(
            field ->
                switch (field) {
                  case "kty", "kid", "alg", "use", "n", "e" -> true;
                  default -> false;
                });
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
