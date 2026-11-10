/* (C) 2026 */

package aros.services.rms.infraestructure.common.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ApiResponse}.
 *
 * <p><b>Feature:</b> Standard success envelope with optional metadata.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>{@code of(data)} creates an envelope with null meta
 *   <li>{@code of(data, meta)} keeps the provided metadata
 *   <li>meta is omitted from JSON when empty and included when present
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Exercises both static factories and asserts their fields directly
 *   <li>Serializes envelopes with Jackson to prove the JSON shape and meta omission
 * </ul>
 */
class ApiResponseTest {

  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void should_create_envelope_without_meta() {
    ApiResponse<String> response = ApiResponse.of("payload");

    assertEquals("payload", response.data());
    assertNull(response.meta());
  }

  @Test
  void should_create_envelope_with_meta() {
    Map<String, Object> meta = Map.of("page", 2);

    ApiResponse<String> response = ApiResponse.of("payload", meta);

    assertEquals("payload", response.data());
    assertSame(meta, response.meta());
  }

  @Test
  void should_omit_meta_from_json_when_empty() throws Exception {
    String json = mapper.writeValueAsString(ApiResponse.of("payload"));

    assertEquals("{\"data\":\"payload\"}", json);
  }

  @Test
  void should_include_meta_in_json_when_present() throws Exception {
    String json = mapper.writeValueAsString(ApiResponse.of("payload", Map.of("page", 2)));

    assertEquals("{\"data\":\"payload\",\"meta\":{\"page\":2}}", json);
  }
}
