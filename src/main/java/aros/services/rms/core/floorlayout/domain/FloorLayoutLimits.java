/* (C) 2026 */

package aros.services.rms.core.floorlayout.domain;

/** Allowed bounds for the editable floor grid. */
public final class FloorLayoutLimits {
  public static final int MIN_COLUMNS = 8;
  public static final int MAX_COLUMNS = 100_000;
  public static final int MIN_ROWS = 6;
  public static final int MAX_ROWS = 100_000;
  public static final int DEFAULT_COLUMNS = 32;
  public static final int DEFAULT_ROWS = 20;
  public static final int MAX_REGION_KEY_LENGTH = 64;

  private FloorLayoutLimits() {}
}
