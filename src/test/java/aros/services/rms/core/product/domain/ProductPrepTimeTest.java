/* (C) 2026 */

package aros.services.rms.core.product.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link ProductPrepTime}. */
class ProductPrepTimeTest {

  @Test
  void shouldUseEstimate_whenModeIsEstimatedEvenWithRealData() {
    ProductPrepTimeStats stats = new ProductPrepTimeStats(14.0, 25, null, null);

    ProductPrepTime result = ProductPrepTime.resolve(10, stats, PrepTimeMode.ESTIMATED);

    assertEquals(10.0, result.usedMinutes());
    assertEquals(PrepTimeMode.ESTIMATED, result.usedSource());
    assertEquals(14.0, result.realMinutes());
  }

  @Test
  void shouldUseReal_whenModeIsRealAndThereAreAtLeastTenSamples() {
    ProductPrepTimeStats stats = new ProductPrepTimeStats(14.0, 10, null, null);

    ProductPrepTime result = ProductPrepTime.resolve(10, stats, PrepTimeMode.REAL);

    assertEquals(14.0, result.usedMinutes());
    assertEquals(PrepTimeMode.REAL, result.usedSource());
  }

  @Test
  void shouldFallBackToEstimate_whenRealHasFewerThanTenSamples() {
    ProductPrepTimeStats stats = new ProductPrepTimeStats(14.0, 9, null, null);

    ProductPrepTime result = ProductPrepTime.resolve(10, stats, PrepTimeMode.REAL);

    assertEquals(10.0, result.usedMinutes());
    assertEquals(PrepTimeMode.ESTIMATED, result.usedSource());
    assertEquals(9, result.realSampleCount());
  }

  @Test
  void shouldUseRealInAutoMode_whenAvailable() {
    ProductPrepTimeStats stats = new ProductPrepTimeStats(12.5, 11, null, null);

    ProductPrepTime result = ProductPrepTime.resolve(10, stats, PrepTimeMode.AUTO);

    assertEquals(12.5, result.usedMinutes());
    assertEquals(PrepTimeMode.REAL, result.usedSource());
  }
}
