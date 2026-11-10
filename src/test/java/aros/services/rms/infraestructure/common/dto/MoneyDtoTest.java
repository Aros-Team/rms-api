/* (C) 2026 */

package aros.services.rms.infraestructure.common.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import aros.services.rms.core.common.money.domain.Money;
import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link MoneyDto} static factories.
 *
 * <p><b>Feature:</b> Conversion of domain/raw monetary values into the standard wire type.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>{@code of(Money)} copies the amount as a plain decimal string and the ISO currency code
 *   <li>{@code of(BigDecimal, String)} keeps the given currency
 *   <li>{@code of(BigDecimal)} defaults to COP
 *   <li>null inputs yield {@code null}
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Calls each factory directly and asserts the rendered amount/currency
 *   <li>Exercises the null paths to prove the helpers are null-safe
 * </ul>
 */
class MoneyDtoTest {

  private static final Currency COP = Currency.getInstance("COP");

  @Test
  void should_copy_amount_and_currency_from_domain_money() {
    Money money = new Money(new BigDecimal("12.50"), COP);

    MoneyDto dto = MoneyDto.of(money);

    assertEquals("12.50", dto.getAmount());
    assertEquals("COP", dto.getCurrency());
  }

  @Test
  void should_return_null_when_domain_money_is_null() {
    assertNull(MoneyDto.of((Money) null));
  }

  @Test
  void should_use_given_currency_for_raw_amount() {
    MoneyDto dto = MoneyDto.of(new BigDecimal("99.90"), "USD");

    assertEquals("99.90", dto.getAmount());
    assertEquals("USD", dto.getCurrency());
  }

  @Test
  void should_default_to_cop_for_raw_amount() {
    MoneyDto dto = MoneyDto.of(new BigDecimal("5"));

    assertEquals("5", dto.getAmount());
    assertEquals(MoneyDto.DEFAULT_CURRENCY, dto.getCurrency());
  }

  @Test
  void should_return_null_when_raw_amount_is_null() {
    assertNull(MoneyDto.of((BigDecimal) null));
    assertNull(MoneyDto.of(null, "COP"));
  }
}
