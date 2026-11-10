/* (C) 2026 */

package aros.services.rms.infraestructure.common.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ApiEnvelopeOpenApiCustomizer}.
 *
 * <p><b>Feature:</b> OpenAPI documentation of the {@link ApiResponse} envelope.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>2xx JSON schemas are replaced by an envelope whose {@code data} property keeps the original
 *       schema
 *   <li>running the customizer twice does not double-wrap
 *   <li>non-2xx responses and non-JSON media types are left untouched
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Builds a minimal in-memory OpenAPI document and runs the customizer
 *   <li>Asserts the resulting schema graph directly, without Spring context
 * </ul>
 */
class ApiEnvelopeOpenApiCustomizerTest {

  private final ApiEnvelopeOpenApiCustomizer customizer = new ApiEnvelopeOpenApiCustomizer();

  @Test
  void should_wrap_success_json_schema_once() {
    StringSchema original = new StringSchema();
    OpenAPI openApi = openApiWith("200", "application/json", original);

    customizer.customise(openApi);

    Schema<?> wrapped = schemaOf(openApi, "200", "application/json");
    ObjectSchema envelope = assertInstanceOf(ObjectSchema.class, wrapped);
    assertSame(original, envelope.getProperties().get("data"));
    Schema<?> meta = envelope.getProperties().get("meta");
    assertEquals(Boolean.TRUE, meta.getAdditionalProperties());
    assertTrue(envelope.getProperties().containsKey("meta"));
  }

  @Test
  void should_not_double_wrap_when_customised_twice() {
    OpenAPI openApi = openApiWith("200", "application/json", new StringSchema());

    customizer.customise(openApi);
    Schema<?> firstWrap = schemaOf(openApi, "200", "application/json");
    customizer.customise(openApi);
    Schema<?> secondWrap = schemaOf(openApi, "200", "application/json");

    assertSame(firstWrap, secondWrap);
  }

  @Test
  void should_leave_error_responses_and_non_json_untouched() {
    StringSchema errorSchema = new StringSchema();
    StringSchema textSchema = new StringSchema();
    OpenAPI openApi = openApiWith("400", "application/json", errorSchema);
    openApi
        .getPaths()
        .get("/ping")
        .getGet()
        .getResponses()
        .addApiResponse(
            "200",
            new io.swagger.v3.oas.models.responses.ApiResponse()
                .content(
                    new Content().addMediaType("text/plain", new MediaType().schema(textSchema))));

    customizer.customise(openApi);

    assertSame(errorSchema, schemaOf(openApi, "400", "application/json"));
    assertSame(textSchema, schemaOf(openApi, "200", "text/plain"));
  }

  @Test
  void should_leave_well_known_paths_and_jwk_set_media_type_untouched() {
    StringSchema wellKnownSchema = new StringSchema();
    OpenAPI wellKnown =
        new OpenAPI()
            .path(
                "/.well-known/jwks.json",
                new PathItem()
                    .get(
                        new Operation()
                            .responses(
                                new ApiResponses()
                                    .addApiResponse(
                                        "200",
                                        new io.swagger.v3.oas.models.responses.ApiResponse()
                                            .content(
                                                new Content()
                                                    .addMediaType(
                                                        "application/jwk-set+json",
                                                        new MediaType()
                                                            .schema(wellKnownSchema)))))));

    customizer.customise(wellKnown);

    assertSame(
        wellKnownSchema,
        wellKnown
            .getPaths()
            .get("/.well-known/jwks.json")
            .getGet()
            .getResponses()
            .get("200")
            .getContent()
            .get("application/jwk-set+json")
            .getSchema());
  }

  @Test
  void should_treat_wildcard_media_types_as_json() {
    assertTrue(customizer.isJsonMediaType("*/*"));
    assertTrue(customizer.isJsonMediaType("application/*"));
  }

  @Test
  void should_exclude_problem_json_and_jwk_set_media_types() {
    assertFalse(customizer.isJsonMediaType("application/problem+json"));
    assertFalse(customizer.isJsonMediaType("application/jwk-set+json"));
  }

  @Test
  void should_wrap_success_schema_published_as_any_media_type() {
    StringSchema original = new StringSchema();
    OpenAPI openApi = openApiWith("200", "*/*", original);

    customizer.customise(openApi);

    ObjectSchema envelope = assertInstanceOf(ObjectSchema.class, schemaOf(openApi, "200", "*/*"));
    assertSame(original, envelope.getProperties().get("data"));
  }

  @Test
  void should_wrap_success_schema_published_as_application_wildcard() {
    StringSchema original = new StringSchema();
    OpenAPI openApi = openApiWith("200", "application/*", original);

    customizer.customise(openApi);

    ObjectSchema envelope =
        assertInstanceOf(ObjectSchema.class, schemaOf(openApi, "200", "application/*"));
    assertSame(original, envelope.getProperties().get("data"));
  }

  private OpenAPI openApiWith(String code, String mediaTypeName, Schema<?> schema) {
    Content content = new Content().addMediaType(mediaTypeName, new MediaType().schema(schema));
    var response = new io.swagger.v3.oas.models.responses.ApiResponse().content(content);
    Operation operation =
        new Operation().responses(new ApiResponses().addApiResponse(code, response));
    return new OpenAPI().path("/ping", new PathItem().get(operation));
  }

  private Schema<?> schemaOf(OpenAPI openApi, String code, String mediaTypeName) {
    return openApi
        .getPaths()
        .get("/ping")
        .getGet()
        .getResponses()
        .get(code)
        .getContent()
        .get(mediaTypeName)
        .getSchema();
  }
}
