/* (C) 2026 */

package aros.services.rms.core.systemconfig.domain;

/** Labor costing strategy used by the product cost engine. */
public enum LaborCostMode {
  /** Use real payroll data when available, otherwise fall back to the standard rate. */
  AUTO,
  /** Use only real payroll data. */
  REAL,
  /** Use the configured standard rate or the worker salary average. */
  STANDARD;

  /**
   * Parses a stored configuration value.
   *
   * @param value the raw value
   * @return the matching mode, or {@code AUTO} when the value is null or unknown
   */
  public static LaborCostMode from(String value) {
    if (value == null || value.isBlank()) {
      return AUTO;
    }
    try {
      return valueOf(value.trim().toUpperCase(java.util.Locale.ROOT));
    } catch (IllegalArgumentException ignored) {
      return AUTO;
    }
  }
}
