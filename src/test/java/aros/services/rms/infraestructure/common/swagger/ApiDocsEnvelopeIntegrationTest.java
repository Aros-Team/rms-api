/* (C) 2026 */

package aros.services.rms.infraestructure.common.swagger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.infraestructure.user.config.AdminInitializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Tests for {@code /v3/api-docs}, the generated OpenAPI document.
 *
 * <p><b>Feature:</b> Published OpenAPI contract for the {@code { data, meta }} success envelope
 * produced by {@code ApiEnvelopeAdvice} (see {@code ApiEnvelopeOpenApiCustomizer}).
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>Every 2xx JSON-ish response (including springdoc's wildcard media types) documents a {@code
 *       data} property
 *   <li>{@code POST /api/auth/login} 200 points {@code data.$ref} at {@code AuthResponse}
 *   <li>List endpoints such as {@code GET /api/v1/tables} are enveloped too
 *   <li>{@code /.well-known/jwks.json} is never enveloped
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Boots the full application context with an H2 in-memory database and generated RSA keys
 *   <li>Requests the generated OpenAPI document through MockMvc, exactly as a client would
 *   <li>Walks every path/operation/2xx/content entry and fails with the offending list when an
 *       enveloped schema is missing
 * </ul>
 */
@SpringBootTest(
    properties = {
      "app.jwt.public-key=unused",
      "app.jwt.private-key=unused",
      "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.datasource.username=sa",
      "spring.datasource.password=sa",
      "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
      "spring.jpa.hibernate.ddl-auto=none",
      "spring.flyway.enabled=false",
      "spring.sql.init.mode=never"
    })
@AutoConfigureMockMvc
class ApiDocsEnvelopeIntegrationTest {

  private static final String ENVELOPE_DATA = "data";

  @MockitoBean private AdminInitializer adminInitializer;

  @Autowired private MockMvc mockMvc;

  private final ObjectMapper mapper = new ObjectMapper();

  @DynamicPropertySource
  static void configureJwtKeys(DynamicPropertyRegistry registry) throws Exception {
    KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
    gen.initialize(2048);
    KeyPair kp = gen.generateKeyPair();
    String pubPem =
        "-----BEGIN PUBLIC KEY-----\n"
            + Base64.getEncoder().encodeToString(kp.getPublic().getEncoded())
            + "\n-----END PUBLIC KEY-----";
    String privPem =
        "-----BEGIN PRIVATE KEY-----\n"
            + Base64.getEncoder().encodeToString(kp.getPrivate().getEncoded())
            + "\n-----END PRIVATE KEY-----";
    registry.add("app.jwt.public-key", () -> pubPem);
    registry.add("app.jwt.private-key", () -> privPem);
  }

  @Test
  void should_document_envelope_for_every_success_json_response() throws Exception {
    JsonNode paths = fetchApiDocs().path("paths");
    assertTrue(paths.isObject() && !paths.isEmpty(), "generated document has no paths");

    List<String> violations = new ArrayList<>();
    int wildcardMediaTypes = 0;
    for (Map.Entry<String, JsonNode> pathEntry : paths.properties()) {
      String path = pathEntry.getKey();
      if (path.startsWith("/.well-known")) {
        continue;
      }
      for (Map.Entry<String, JsonNode> operationEntry : pathEntry.getValue().properties()) {
        JsonNode responses = operationEntry.getValue().path("responses");
        if (!responses.isObject()) {
          continue;
        }
        for (Map.Entry<String, JsonNode> responseEntry : responses.properties()) {
          if (!responseEntry.getKey().startsWith("2")) {
            continue;
          }
          JsonNode content = responseEntry.getValue().path("content");
          if (!content.isObject()) {
            continue;
          }
          for (Map.Entry<String, JsonNode> mediaEntry : content.properties()) {
            if (!isJsonMediaType(mediaEntry.getKey())) {
              continue;
            }
            if ("*/*".equals(mediaEntry.getKey()) || "application/*".equals(mediaEntry.getKey())) {
              wildcardMediaTypes++;
            }
            JsonNode properties = mediaEntry.getValue().path("schema").path("properties");
            if (!properties.isObject() || !properties.has(ENVELOPE_DATA)) {
              violations.add(
                  operationEntry.getKey().toUpperCase(Locale.ROOT)
                      + " "
                      + path
                      + " -> "
                      + responseEntry.getKey()
                      + " "
                      + mediaEntry.getKey());
            }
          }
        }
      }
    }

    assertTrue(
        violations.isEmpty(),
        () ->
            "2xx responses missing the envelope 'data' property:\n"
                + String.join("\n", violations));
    assertTrue(
        wildcardMediaTypes > 0,
        "expected springdoc to publish at least one */* or application/* 2xx media type");
  }

  @Test
  void should_document_login_response_as_enveloped_auth_response() throws Exception {
    JsonNode login200 = fetchApiDocs().path("paths").path("/api/auth/login").path("post");
    JsonNode content = login200.path("responses").path("200").path("content");

    assertTrue(content.isObject() && !content.isEmpty(), "login 200 has no documented content");
    assertTrue(
        content.has("*/*"),
        "springdoc no longer publishes the wildcard media type for /api/auth/login; "
            + "this test exists to guard that path");

    JsonNode wrappedMedia = null;
    for (Map.Entry<String, JsonNode> mediaEntry : content.properties()) {
      if (isJsonMediaType(mediaEntry.getKey())) {
        wrappedMedia = mediaEntry.getValue();
        break;
      }
    }
    assertTrue(wrappedMedia != null, "login 200 has no JSON media type");

    JsonNode data = wrappedMedia.path("schema").path("properties").path(ENVELOPE_DATA);
    assertEquals("#/components/schemas/AuthResponse", data.path("$ref").asText());
  }

  @Test
  void should_document_list_endpoint_as_enveloped() throws Exception {
    JsonNode content =
        fetchApiDocs()
            .path("paths")
            .path("/api/v1/tables")
            .path("get")
            .path("responses")
            .path("200")
            .path("content");

    assertTrue(content.isObject() && !content.isEmpty(), "tables 200 has no documented content");
    boolean enveloped =
        content.properties().stream()
            .filter(media -> isJsonMediaType(media.getKey()))
            .anyMatch(
                media -> media.getValue().path("schema").path("properties").has(ENVELOPE_DATA));
    assertTrue(enveloped, "GET /api/v1/tables 200 is not documented with the envelope");
  }

  @Test
  void should_not_document_envelope_for_well_known_jwks() throws Exception {
    JsonNode jwks = fetchApiDocs().path("paths").path("/.well-known/jwks.json").path("get");
    JsonNode content = jwks.path("responses").path("200").path("content");

    assertFalse(content.isEmpty(), "JWKS 200 should be documented as raw content");
    assertTrue(
        content.has("application/jwk-set+json"),
        "JWKS must be documented under application/jwk-set+json");
    boolean enveloped =
        content.properties().stream()
            .anyMatch(
                media -> media.getValue().path("schema").path("properties").has(ENVELOPE_DATA));
    assertFalse(enveloped, "JWKS response must stay a raw JWK Set, not an envelope");
  }

  private JsonNode fetchApiDocs() throws Exception {
    MvcResult result = mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andReturn();
    return mapper.readTree(result.getResponse().getContentAsString());
  }

  private boolean isJsonMediaType(String mediaType) {
    String normalized = mediaType.toLowerCase(Locale.ROOT);
    if ("application/jwk-set+json".equals(normalized)
        || "application/problem+json".equals(normalized)) {
      return false;
    }
    return "application/json".equals(normalized)
        || "*/*".equals(normalized)
        || "application/*".equals(normalized)
        || normalized.endsWith("+json");
  }
}
