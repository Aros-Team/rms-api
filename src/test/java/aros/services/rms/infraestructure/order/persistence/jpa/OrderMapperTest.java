/* (C) 2026 */

package aros.services.rms.infraestructure.order.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import aros.services.rms.core.order.domain.Order;
import aros.services.rms.infraestructure.order.persistence.OrderStatus;
import aros.services.rms.infraestructure.product.persistence.jpa.ProductMapper;
import aros.services.rms.infraestructure.table.persistence.jpa.TableMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Null-safety tests for {@link OrderMapper} covering the three columns added in V32: party size,
 * open time, and close time. These fields must round-trip cleanly when null (back-compat with rows
 * created before V32) and when populated.
 */
@ExtendWith(MockitoExtension.class)
class OrderMapperTest {

  private static final LocalDateTime OPEN_TIME = LocalDateTime.of(2026, 7, 17, 11, 0);
  private static final LocalDateTime CLOSE_TIME = LocalDateTime.of(2026, 7, 17, 23, 0);

  @Mock private TableMapper tableMapper;
  @Mock private ProductMapper productMapper;

  private OrderMapper orderMapper;

  @BeforeEach
  void setUp() {
    orderMapper = new OrderMapper(tableMapper, productMapper);
  }

  // ---------------------------------------------------------------------------
  // N-01: toDomain preserves null new fields without throwing NPE
  // ---------------------------------------------------------------------------

  @Test
  void toDomain_should_preserveNullNewFields() {
    aros.services.rms.infraestructure.order.persistence.Order entity =
        aros.services.rms.infraestructure.order.persistence.Order.builder()
            .id(7L)
            .status(OrderStatus.QUEUE)
            .partySize(null)
            .openTime(null)
            .closeTime(null)
            .build();

    Order domain = orderMapper.toDomain(entity);

    assertEquals(7L, domain.getId());
    assertNull(domain.getPartySize());
    assertNull(domain.getOpenTime());
    assertNull(domain.getCloseTime());
  }

  // ---------------------------------------------------------------------------
  // N-02: toEntity preserves null new fields without throwing NPE
  // ---------------------------------------------------------------------------

  @Test
  void toEntity_should_preserveNullNewFields() {
    Order domain =
        Order.builder()
            .id(7L)
            .status(aros.services.rms.core.order.domain.OrderStatus.QUEUE)
            .partySize(null)
            .openTime(null)
            .closeTime(null)
            .build();

    aros.services.rms.infraestructure.order.persistence.Order entity = orderMapper.toEntity(domain);

    assertEquals(7L, entity.getId());
    assertNull(entity.getPartySize());
    assertNull(entity.getOpenTime());
    assertNull(entity.getCloseTime());
  }

  // ---------------------------------------------------------------------------
  // N-03: round-trip preserves set new fields
  // ---------------------------------------------------------------------------

  @Test
  void roundTrip_should_preserveSetNewFields() {
    aros.services.rms.infraestructure.order.persistence.Order entity =
        aros.services.rms.infraestructure.order.persistence.Order.builder()
            .id(11L)
            .status(OrderStatus.QUEUE)
            .partySize(4)
            .openTime(OPEN_TIME)
            .closeTime(CLOSE_TIME)
            .build();

    aros.services.rms.infraestructure.order.persistence.Order roundTripped =
        orderMapper.toEntity(orderMapper.toDomain(entity));

    assertEquals(4, roundTripped.getPartySize());
    assertEquals(OPEN_TIME, roundTripped.getOpenTime());
    assertEquals(CLOSE_TIME, roundTripped.getCloseTime());
  }

  // ---------------------------------------------------------------------------
  // N-04: round-trip preserves null new fields
  // ---------------------------------------------------------------------------

  @Test
  void roundTrip_should_preserveNullNewFields() {
    aros.services.rms.infraestructure.order.persistence.Order entity =
        aros.services.rms.infraestructure.order.persistence.Order.builder()
            .id(12L)
            .status(OrderStatus.QUEUE)
            .partySize(null)
            .openTime(null)
            .closeTime(null)
            .build();

    aros.services.rms.infraestructure.order.persistence.Order roundTripped =
        orderMapper.toEntity(orderMapper.toDomain(entity));

    assertNull(roundTripped.getPartySize());
    assertNull(roundTripped.getOpenTime());
    assertNull(roundTripped.getCloseTime());
  }
}
