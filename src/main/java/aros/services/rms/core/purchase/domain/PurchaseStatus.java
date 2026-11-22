/* (C) 2026 */

package aros.services.rms.core.purchase.domain;

import java.util.Locale;

/**
 * Lifecycle status of a purchase order.
 *
 * <p>{@link #CONFIRMED} means the order is placed but no goods have been received yet. {@link
 * #RECEIVED} means at least one line arrived and entered stock. The domain currently has no
 * cancellation flow, so there is no terminal {@code CANCELLED} value.
 *
 * <p>Before V61 the status lived only as a token at the start of {@code notes} ({@code RECIBIDA} /
 * {@code CONFIRMADA}). {@link #fromPersisted(String)} still accepts those legacy tokens so older
 * rows and pre-migration fixtures keep reading correctly.
 */
public enum PurchaseStatus {
  CONFIRMED,
  RECEIVED;

  /**
   * Parses a persisted status value, accepting the legacy notes token spellings.
   *
   * @param value the persisted status (or legacy token); may be null
   * @return the matching status, {@link #CONFIRMED} for a null/blank value
   * @throws IllegalArgumentException if the value is not a known status
   */
  public static PurchaseStatus fromPersisted(String value) {
    if (value == null || value.isBlank()) {
      return CONFIRMED;
    }
    return switch (value.trim().toUpperCase(Locale.ROOT)) {
      case "CONFIRMED", "CONFIRMADA" -> CONFIRMED;
      case "RECEIVED", "RECIBIDA" -> RECEIVED;
      default -> throw new IllegalArgumentException("Unknown purchase status: " + value);
    };
  }
}
