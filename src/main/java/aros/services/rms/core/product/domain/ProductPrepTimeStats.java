/* (C) 2026 */

package aros.services.rms.core.product.domain;

import java.time.LocalDateTime;

/**
 * Measured preparation-time statistics for one product.
 *
 * <p>Built from {@code order_details.preparation_started_at} and {@code ready_at}; a line only
 * contributes when both timestamps exist and the duration is sane.
 *
 * @param averageMinutes average measured line duration in minutes
 * @param sampleCount number of valid measured lines
 * @param firstSampleAt oldest valid sample
 * @param lastSampleAt newest valid sample
 */
public record ProductPrepTimeStats(
    Double averageMinutes,
    long sampleCount,
    LocalDateTime firstSampleAt,
    LocalDateTime lastSampleAt) {

  /** Empty statistics used when the product has never been measured. */
  public static ProductPrepTimeStats empty() {
    return new ProductPrepTimeStats(null, 0, null, null);
  }
}
