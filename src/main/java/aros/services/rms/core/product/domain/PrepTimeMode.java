/* (C) 2026 */

package aros.services.rms.core.product.domain;

/**
 * Selects which preparation time feeds the product labor cost.
 *
 * <ul>
 *   <li>{@link #ESTIMATED} — always use the editable base estimate. Default.
 *   <li>{@link #REAL} — use the measured average when enough samples exist, otherwise fall back to
 *       the estimate.
 *   <li>{@link #AUTO} — same availability rule as REAL, intended for products whose estimate no
 *       longer needs peak-hour protection.
 * </ul>
 */
public enum PrepTimeMode {
  ESTIMATED,
  REAL,
  AUTO
}
