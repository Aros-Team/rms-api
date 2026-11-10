/* (C) 2026 */

package aros.services.rms.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderDetail;
import aros.services.rms.core.order.domain.OrderStatus;
import aros.services.rms.infraestructure.common.dto.MoneyDto;
import aros.services.rms.infraestructure.order.api.dto.OrderResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link OrderResponse} total computation.
 *
 * <p><b>Feature:</b> Server-computed order {@code total} field on {@link OrderResponse}.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>Multiple details → total is the sum of their unit prices, in the first unit price currency
 *   <li>Empty or null details → total is {@code 0.00} in {@code COP}
 *   <li>Details with a null unit price are skipped
 *   <li>Extra charge is not added again because the unit price already includes it
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Builds domain {@link Order}/{@link OrderDetail} instances directly (no mocks needed)
 *   <li>Calls {@link OrderResponse#fromDomain(Order)} and asserts the exact {@link MoneyDto} total
 *   <li>Also exercises the shared {@link OrderResponse#calculateTotal(List)} helper
 * </ul>
 */
class OrderResponseTest {

  private static final Currency COP = Currency.getInstance("COP");

  private static Money money(String amount) {
    return new Money(new BigDecimal(amount), COP);
  }

  private static OrderDetail detail(String unitPrice, String extraCharge) {
    return OrderDetail.builder()
        .id(1L)
        .unitPrice(unitPrice != null ? money(unitPrice) : null)
        .extraCharge(extraCharge != null ? money(extraCharge) : null)
        .build();
  }

  private static Order orderWith(List<OrderDetail> details) {
    return Order.builder()
        .id(1L)
        .date(LocalDateTime.of(2026, 3, 8, 14, 30))
        .status(OrderStatus.QUEUE)
        .details(details)
        .build();
  }

  @Test
  void should_sum_unit_prices_when_multiple_details() {
    // Arrange
    Order order = orderWith(List.of(detail("12.50", "0.00"), detail("25.00", "0.00")));

    // Act
    OrderResponse response = OrderResponse.fromDomain(order);

    // Assert
    assertNotNull(response.total());
    assertEquals("37.50", response.total().getAmount());
    assertEquals("COP", response.total().getCurrency());
  }

  @Test
  void should_return_zero_cop_when_no_details() {
    // Arrange
    Order order = orderWith(List.of());

    // Act
    OrderResponse response = OrderResponse.fromDomain(order);

    // Assert
    assertNotNull(response.total());
    assertEquals("0.00", response.total().getAmount());
    assertEquals("COP", response.total().getCurrency());
  }

  @Test
  void should_return_zero_cop_when_details_are_null() {
    // Arrange
    Order order = orderWith(null);

    // Act
    OrderResponse response = OrderResponse.fromDomain(order);

    // Assert
    assertNotNull(response.total());
    assertEquals("0.00", response.total().getAmount());
    assertEquals("COP", response.total().getCurrency());
  }

  @Test
  void should_skip_details_with_null_unit_price() {
    // Arrange
    Order order = orderWith(List.of(detail(null, null), detail("10.00", "0.00")));

    // Act
    OrderResponse response = OrderResponse.fromDomain(order);

    // Assert
    assertEquals("10.00", response.total().getAmount());
    assertEquals("COP", response.total().getCurrency());
  }

  @Test
  void should_not_double_count_extra_charge() {
    // Arrange: unitPrice already includes the 2.50 surcharge (base 10 + extra 2.50)
    Order order = orderWith(List.of(detail("12.50", "2.50")));

    // Act
    OrderResponse response = OrderResponse.fromDomain(order);

    // Assert
    assertEquals("12.50", response.total().getAmount());
    assertEquals("COP", response.total().getCurrency());
  }

  @Test
  void should_compute_total_from_calculate_total_helper() {
    // Arrange
    List<OrderDetail> details = List.of(detail("7.25", null), detail("2.75", null));

    // Act
    MoneyDto total = OrderResponse.calculateTotal(details);

    // Assert
    assertEquals("10.00", total.getAmount());
    assertEquals("COP", total.getCurrency());
  }
}
