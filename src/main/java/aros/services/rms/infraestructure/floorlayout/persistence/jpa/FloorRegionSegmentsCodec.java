/* (C) 2026 */

package aros.services.rms.infraestructure.floorlayout.persistence.jpa;

import aros.services.rms.core.floorlayout.domain.FloorSegment;
import java.util.List;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Serializes the compact segment list stored in {@code floor_layout_region.segments_json}.
 *
 * <p>Jackson 3 is the HTTP mapper in this application; using its {@link JsonMapper} directly keeps
 * the codec independent from the Spring context and from the legacy Jackson 2 mapper still present
 * for a few older services.
 */
@Component
public class FloorRegionSegmentsCodec {

  private static final JsonMapper MAPPER = JsonMapper.builder().build();
  private static final TypeReference<List<FloorSegment>> SEGMENT_LIST = new TypeReference<>() {};

  /**
   * Serializes canonical segments.
   *
   * @param segments canonical segment list
   * @return compact JSON array
   */
  public String encode(List<FloorSegment> segments) {
    return MAPPER.writeValueAsString(segments == null ? List.of() : segments);
  }

  /**
   * Deserializes stored segments.
   *
   * @param json stored JSON array
   * @return segment list
   * @throws IllegalStateException when the stored payload is malformed
   */
  public List<FloorSegment> decode(String json) {
    if (json == null || json.isBlank()) {
      return List.of();
    }
    try {
      List<FloorSegment> segments = MAPPER.readValue(json, SEGMENT_LIST);
      return segments == null ? List.of() : segments;
    } catch (RuntimeException exception) {
      throw new IllegalStateException(
          "La geometría almacenada de la distribución es inválida", exception);
    }
  }
}
