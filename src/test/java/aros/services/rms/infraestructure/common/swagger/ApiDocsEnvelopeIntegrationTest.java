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

  @Test
  void should_document_kitchen_clock_settings_and_purchase_status() throws Exception {
    JsonNode document = fetchApiDocs();
    JsonNode paths = document.path("paths");

    List<String> requiredPaths =
        List.of(
            "/api/v1/orders/{id}/prepare",
            "/api/v1/orders/{id}/details/{detailId}/ready",
            "/api/v1/workers/me/clock-in",
            "/api/v1/workers/me/clock-out",
            "/api/v1/settings/business",
            "/api/v1/settings/currency",
            "/api/v1/settings/labor",
            "/api/v1/settings/notifications",
            "/api/v1/purchases",
            "/api/v1/purchases/{id}");
    List<String> missing = requiredPaths.stream().filter(path -> !paths.has(path)).toList();
    assertTrue(missing.isEmpty(), "OpenAPI is missing hardened endpoints: " + missing);

    JsonNode purchaseStatus =
        document
            .path("components")
            .path("schemas")
            .path("PurchaseOrderResponse")
            .path("properties")
            .path("status");
    assertEquals("string", purchaseStatus.path("type").asText());
    assertTrue(purchaseStatus.path("enum").isArray(), "purchase status must be a typed enum");

    JsonNode order200 =
        paths.path("/api/v1/orders").path("get").path("responses").path("200").path("content");
    boolean envelopeDocumented =
        order200.properties().stream()
            .filter(media -> isJsonMediaType(media.getKey()))
            .anyMatch(media -> media.getValue().path("schema").path("properties").has("data"));
    assertTrue(envelopeDocumented, "GET /api/v1/orders 200 is not documented with the envelope");
  }

  @Test
  void should_document_payment_response_money_as_money_dto() throws Exception {
    JsonNode document = fetchApiDocs();
    JsonNode schemas = document.path("components").path("schemas");

    JsonNode money = schemas.path("MoneyDto");
    assertEquals("string", money.path("properties").path("amount").path("type").asText());
    assertEquals("string", money.path("properties").path("currency").path("type").asText());

    JsonNode payment = schemas.path("PaymentResponse").path("properties");
    assertMoneyRef(payment, "subtotal");
    assertMoneyRef(payment, "discount");
    assertMoneyRef(payment, "tip");
    assertMoneyRef(payment, "total");
    assertMoneyRef(payment, "tendered");
    assertMoneyRef(payment, "change");
    assertMoneyRef(schemas.path("TenderResponse").path("properties"), "amount");

    JsonNode summary = schemas.path("PaymentSummaryResponse").path("properties");
    assertMoneyRef(summary, "subtotal");
    assertMoneyRef(summary, "discount");
    assertMoneyRef(summary, "tip");
    assertMoneyRef(summary, "total");

    assertMoneyRef(schemas.path("PendingOrderResponse").path("properties"), "total");

    JsonNode paths = document.path("paths");
    assertEquals(
        "#/components/schemas/PaymentResponse",
        paths
            .path("/api/v1/payments/{id}")
            .path("get")
            .path("responses")
            .path("200")
            .path("content")
            .path("*/*")
            .path("schema")
            .path("properties")
            .path("data")
            .path("$ref")
            .asText());
    assertEquals(
        "#/components/schemas/PaymentResponse",
        paths
            .path("/api/v1/payments")
            .path("post")
            .path("responses")
            .path("201")
            .path("content")
            .path("*/*")
            .path("schema")
            .path("properties")
            .path("data")
            .path("$ref")
            .asText());

    // Request money stays a primitive input but the published scalar must match the client:
    // decimal strings, coerced to BigDecimal by Jackson.
    JsonNode request = schemas.path("RegisterPaymentRequest").path("properties");
    assertEquals("string", request.path("tipAmount").path("type").asText());
    assertEquals(
        "string",
        schemas.path("TenderRequest").path("properties").path("amount").path("type").asText());
    assertEquals(
        "string",
        schemas.path("DiscountRequest").path("properties").path("value").path("type").asText());
    assertEquals(
        "string",
        schemas
            .path("OpenCashShiftRequest")
            .path("properties")
            .path("openingFloat")
            .path("type")
            .asText());
    assertEquals(
        "string",
        schemas
            .path("CloseCashShiftRequest")
            .path("properties")
            .path("countedCash")
            .path("type")
            .asText());
  }

  /** Asserts that a property, or a direct child of an items node, refs {@code MoneyDto}. */
  private void assertMoneyRef(JsonNode node, String field) {
    JsonNode target = field == null ? node : node.path(field);
    assertEquals(
        "#/components/schemas/MoneyDto",
        target.path("$ref").asText(),
        "payment money field is not a MoneyDto ref: " + field);
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
