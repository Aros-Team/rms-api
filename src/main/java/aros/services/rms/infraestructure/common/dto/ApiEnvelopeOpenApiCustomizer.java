/* (C) 2026 */

package aros.services.rms.infraestructure.common.dto;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Locale;
import java.util.Map;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Rewrites generated OpenAPI success responses to document the {@link ApiResponse} envelope.
 *
 * <p>Active only when {@code app.api.envelope.enabled=true} (the default), so the published
 * contract matches the runtime behavior. Every 2xx JSON response schema that is not already an
 * envelope is replaced by an object schema with a {@code data} property holding the original schema
 * and a free-form {@code meta} object property. {@code /.well-known} paths and the {@code
 * application/jwk-set+json} media type are left untouched, mirroring {@link ApiEnvelopeAdvice}.
 */
@Component
@ConditionalOnProperty(
    name = "app.api.envelope.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class ApiEnvelopeOpenApiCustomizer implements OpenApiCustomizer {

  private static final String JSON_MEDIA_TYPE = "application/json";
  private static final String ANY_MEDIA_TYPE = "*/*";
  private static final String APPLICATION_WILDCARD_MEDIA_TYPE = "application/*";
  private static final String JWKS_MEDIA_TYPE = "application/jwk-set+json";
  private static final String PROBLEM_MEDIA_TYPE = "application/problem+json";
  private static final String WELL_KNOWN_PATH_PREFIX = "/.well-known";

  /** {@inheritDoc} */
  @Override
  public void customise(OpenAPI openApi) {
    if (openApi.getPaths() == null) {
      return;
    }
    for (var entry : openApi.getPaths().entrySet()) {
      if (isExcludedPath(entry.getKey())) {
        continue;
      }
      for (Operation operation : entry.getValue().readOperations()) {
        wrapResponses(operation);
      }
    }
  }

  private void wrapResponses(Operation operation) {
    if (operation.getResponses() == null) {
      return;
    }
    for (var entry : operation.getResponses().entrySet()) {
      if (!isSuccessCode(entry.getKey())) {
        continue;
      }
      var response = entry.getValue();
      if (response == null || response.getContent() == null) {
        continue;
      }
      for (Map.Entry<String, MediaType> content : response.getContent().entrySet()) {
        if (!isJsonMediaType(content.getKey())) {
          continue;
        }
        MediaType mediaType = content.getValue();
        Schema<?> schema = mediaType.getSchema();
        if (schema == null || isEnvelope(schema)) {
          continue;
        }
        mediaType.setSchema(wrap(schema));
      }
    }
  }

  private boolean isSuccessCode(String code) {
    return code != null && code.startsWith("2");
  }

  /**
   * Tells whether a media type carries a JSON body that should be enveloped.
   *
   * <p>Besides {@code application/json} and any {@code *+json} type, wildcard media types are
   * treated as JSON: generated operations without an explicit {@code produces} are published by
   * springdoc with a wildcard media type, even though the runtime advice still wraps them as JSON.
   * Ignoring those wildcards would let the published spec diverge from the real response. Signed
   * JWK Sets and RFC 7807 problem documents stay untouched.
   *
   * @param mediaType the media type key from an OpenAPI response, may be {@code null}
   * @return {@code true} when the body must be documented inside the envelope
   */
  boolean isJsonMediaType(String mediaType) {
    if (mediaType == null) {
      return false;
    }
    String normalized = mediaType.trim().toLowerCase(Locale.ROOT);
    if (JWKS_MEDIA_TYPE.equals(normalized) || PROBLEM_MEDIA_TYPE.equals(normalized)) {
      return false;
    }
    return JSON_MEDIA_TYPE.equals(normalized)
        || ANY_MEDIA_TYPE.equals(normalized)
        || APPLICATION_WILDCARD_MEDIA_TYPE.equals(normalized)
        || normalized.endsWith("+json");
  }

  private boolean isExcludedPath(String path) {
    return path != null && path.startsWith(WELL_KNOWN_PATH_PREFIX);
  }

  private boolean isEnvelope(Schema<?> schema) {
    return schema.getProperties() != null && schema.getProperties().containsKey("data");
  }

  private Schema<?> wrap(Schema<?> original) {
    ObjectSchema meta = new ObjectSchema();
    meta.setAdditionalProperties(true);
    meta.setDescription("Optional response metadata");

    ObjectSchema envelope = new ObjectSchema();
    envelope.setDescription("Standard success response envelope");
    envelope.addProperty("data", original);
    envelope.addProperty("meta", meta);
    return envelope;
  }
}
