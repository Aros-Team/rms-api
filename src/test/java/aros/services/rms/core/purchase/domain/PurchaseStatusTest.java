/* (C) 2026 */

package aros.services.rms.core.purchase.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link PurchaseStatus}.
 *
 * <p><b>Feature:</b> Typed purchase-order status introduced by V61.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>Typed values parse to their matching enum constant
 *   <li>Legacy notes tokens (RECIBIDA / CONFIRMADA) still parse
 *   <li>A null or blank value falls back to CONFIRMED
 *   <li>An unknown value is rejected instead of silently corrupted
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Calls {@link PurchaseStatus#fromPersisted(String)} with each persisted spelling
 *   <li>Asserts the exact enum constant or the thrown exception
 * </ul>
 */
class PurchaseStatusTest {

  @Test
  void should_parse_typed_status_values() {
    assertEquals(PurchaseStatus.RECEIVED, PurchaseStatus.fromPersisted("RECEIVED"));
    assertEquals(PurchaseStatus.CONFIRMED, PurchaseStatus.fromPersisted("CONFIRMED"));
  }

  @Test
  void should_parse_legacy_notes_tokens() {
    assertEquals(PurchaseStatus.RECEIVED, PurchaseStatus.fromPersisted("RECIBIDA"));
    assertEquals(PurchaseStatus.CONFIRMED, PurchaseStatus.fromPersisted("CONFIRMADA"));
  }

  @Test
  void should_default_to_confirmed_when_status_is_blank() {
    assertEquals(PurchaseStatus.CONFIRMED, PurchaseStatus.fromPersisted(null));
    assertEquals(PurchaseStatus.CONFIRMED, PurchaseStatus.fromPersisted("  "));
  }

  @Test
  void should_reject_unknown_status() {
    assertThrows(IllegalArgumentException.class, () -> PurchaseStatus.fromPersisted("CANCELLED"));
  }
}
