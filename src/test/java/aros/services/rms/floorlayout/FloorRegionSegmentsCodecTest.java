/* (C) 2026 */

package aros.services.rms.floorlayout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import aros.services.rms.core.floorlayout.domain.FloorSegment;
import aros.services.rms.infraestructure.floorlayout.persistence.jpa.FloorRegionSegmentsCodec;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link FloorRegionSegmentsCodec}.
 *
 * <p><b>Feature:</b> compact geometry persistence.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>Round-trips canonical row segments through the stored JSON
 *   <li>Rejects malformed stored geometry instead of returning a partial layout
 * </ul>
 */
class FloorRegionSegmentsCodecTest {

  private final FloorRegionSegmentsCodec codec = new FloorRegionSegmentsCodec();

  @Test
  void shouldRoundTripCanonicalSegments() {
    List<FloorSegment> segments = List.of(new FloorSegment(0, 0, 3), new FloorSegment(2, 5, 8));

    String json = codec.encode(segments);

    assertEquals("[{\"row\":0,\"from\":0,\"to\":3},{\"row\":2,\"from\":5,\"to\":8}]", json);
    assertEquals(segments, codec.decode(json));
  }

  @Test
  void shouldRejectMalformedStoredGeometry() {
    assertThrows(IllegalStateException.class, () -> codec.decode("{not-json"));
  }
}
