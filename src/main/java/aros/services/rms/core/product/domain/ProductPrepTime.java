/* (C) 2026 */

package aros.services.rms.core.product.domain;

/**
 * Resolved preparation time for costing.
 *
 * @param estimatedMinutes editable base estimate
 * @param realMinutes measured average, or null when no data exists
 * @param realSampleCount number of valid measured lines
 * @param mode configured policy
 * @param usedMinutes minutes that feed the labor formula
 * @param usedSource source actually used ({@link PrepTimeMode#ESTIMATED} or {@link
 *     PrepTimeMode#REAL})
 */
public record ProductPrepTime(
    Integer estimatedMinutes,
    Double realMinutes,
    long realSampleCount,
    PrepTimeMode mode,
    Double usedMinutes,
    PrepTimeMode usedSource) {

  /** Minimum valid samples before real time can be offered to costing. */
  public static final long MIN_REAL_SAMPLES = 10;

  /**
   * Resolves which preparation time feeds the cost.
   *
   * @param estimatedMinutes editable estimate
   * @param stats measured statistics
   * @param mode configured policy
   * @return the resolved prep time
   */
  public static ProductPrepTime resolve(
      Integer estimatedMinutes, ProductPrepTimeStats stats, PrepTimeMode mode) {
    PrepTimeMode safeMode = mode == null ? PrepTimeMode.ESTIMATED : mode;
    double estimated =
        estimatedMinutes == null || estimatedMinutes <= 0 ? 0.0 : estimatedMinutes.doubleValue();
    ProductPrepTimeStats safeStats = stats == null ? ProductPrepTimeStats.empty() : stats;
    boolean realAvailable =
        safeStats.sampleCount() >= MIN_REAL_SAMPLES
            && safeStats.averageMinutes() != null
            && safeStats.averageMinutes() > 0;
    if (safeMode != PrepTimeMode.ESTIMATED && realAvailable) {
      return new ProductPrepTime(
          estimatedMinutes,
          safeStats.averageMinutes(),
          safeStats.sampleCount(),
          safeMode,
          safeStats.averageMinutes(),
          PrepTimeMode.REAL);
    }
    return new ProductPrepTime(
        estimatedMinutes,
        safeStats.averageMinutes(),
        safeStats.sampleCount(),
        safeMode,
        estimated,
        PrepTimeMode.ESTIMATED);
  }
}
