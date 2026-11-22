/* (C) 2026 */

package aros.services.rms.infraestructure.purchase.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.purchase.domain.PurchaseOrder;
import aros.services.rms.core.purchase.domain.PurchaseStatus;
import aros.services.rms.infraestructure.purchase.persistence.PurchaseOrderEntity;
import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link PurchaseOrderMapper}: the typed status (V61) round-trip.
 *
 * <p><b>Feature:</b> The purchase response must expose the persisted lifecycle status.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>RECEIVED and CONFIRMED survive domain -> entity -> domain
 *   <li>A domain without an explicit status persists as CONFIRMED
 *   <li>An entity without a status reads as CONFIRMED (pre-migration fixtures)
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Maps a minimal purchase order through the real mapper in both directions
 *   <li>Asserts the exact enum constant on each side
 * </ul>
 */
class PurchaseOrderMapperTest {

  private static final Currency COP = Currency.getInstance("COP");
  private final PurchaseOrderMapper mapper = new PurchaseOrderMapper(new PurchaseOrderItemMapper());

  @Test
  void should_map_received_status_to_entity_and_back() {
    PurchaseOrder domain =
        PurchaseOrder.builder()
            .id(7L)
            .totalAmount(new Money(BigDecimal.ZERO, COP))
            .status(PurchaseStatus.RECEIVED)
            .build();

    PurchaseOrderEntity entity = mapper.toEntity(domain);
    PurchaseOrder roundTripped = mapper.toDomain(entity, COP);

    assertEquals(PurchaseStatus.RECEIVED, entity.getStatus());
    assertEquals(PurchaseStatus.RECEIVED, roundTripped.getStatus());
  }

  @Test
  void should_persist_confirmed_when_domain_status_is_null() {
    PurchaseOrder domain =
        PurchaseOrder.builder().id(8L).totalAmount(new Money(BigDecimal.ZERO, COP)).build();
    domain.setStatus(null);

    assertEquals(PurchaseStatus.CONFIRMED, mapper.toEntity(domain).getStatus());
  }

  @Test
  void should_read_confirmed_when_entity_status_is_null() {
    PurchaseOrderEntity entity =
        PurchaseOrderEntity.builder().id(9L).totalAmount(BigDecimal.ZERO).build();
    entity.setStatus(null);

    assertEquals(PurchaseStatus.CONFIRMED, mapper.toDomain(entity, COP).getStatus());
  }
}
