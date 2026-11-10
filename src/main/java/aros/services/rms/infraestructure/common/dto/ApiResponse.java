/* (C) 2026 */

package aros.services.rms.infraestructure.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

/**
 * Standard success envelope for every REST response.
 *
 * <p>The payload is carried in {@code data} and optional metadata (pagination, tracing, etc.) in
 * {@code meta}. When {@code meta} is {@code null} or empty it is omitted from the JSON output.
 * Error responses are RFC 7807 {@code application/problem+json} documents and are never wrapped in
 * this envelope.
 *
 * @param <T> the payload type
 * @param data the response payload
 * @param meta optional response metadata
 */
@Schema(description = "Standard success response envelope")
public record ApiResponse<T>(
    @Schema(description = "Response payload") T data,
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @Schema(description = "Optional response metadata (omitted when empty)")
        Map<String, Object> meta) {

  /**
   * Creates an envelope with no metadata.
   *
   * @param data the response payload
   * @param <T> the payload type
   * @return a new envelope
   */
  public static <T> ApiResponse<T> of(T data) {
    return new ApiResponse<>(data, null);
  }

  /**
   * Creates an envelope with metadata.
   *
   * @param data the response payload
   * @param meta the response metadata, may be {@code null}
   * @param <T> the payload type
   * @return a new envelope
   */
  public static <T> ApiResponse<T> of(T data, Map<String, Object> meta) {
    return new ApiResponse<>(data, meta);
  }
}
