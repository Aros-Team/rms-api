/* (C) 2026 */

package aros.services.rms.core.common.money;

import static org.junit.jupiter.api.Assertions.assertEquals;

import aros.services.rms.core.common.money.application.MoneyCalculator;
import aros.services.rms.core.common.money.domain.Money;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.Test;

class MoneyCalculatorTest {

  private static final Currency COP = Currency.getInstance("COP");

  @Test
  void weightedAverageWithKnownValues() {
    Money current = Money.of("10.00", COP);
    BigDecimal currentQty = new BigDecimal("2");
    Money purchased = Money.of("12.00", COP);
    BigDecimal purchasedQty = new BigDecimal("3");

    Money avg = MoneyCalculator.weightedAverage(current, currentQty, purchased, purchasedQty);

    Money expected = Money.of("11.20", COP);
    assertEquals(expected.amount(), avg.amount());
    assertEquals(expected.currency(), avg.currency());
  }

  @Test
  void weightedAverageSamePrice() {
    Money price = Money.of("10.00", COP);
    Money avg = MoneyCalculator.weightedAverage(price, BigDecimal.ONE, price, BigDecimal.ONE);
    assertEquals(Money.of("10.00", COP), avg);
  }

  @Test
  void splitEvenly3PartsFrom10() {
    List<Money> parts = MoneyCalculator.splitEvenly(Money.of("10.00", COP), 3);
    assertEquals(3, parts.size());
    assertEquals(Money.of("3.34", COP), parts.get(0));
    assertEquals(Money.of("3.33", COP), parts.get(1));
    assertEquals(Money.of("3.33", COP), parts.get(2));
    BigDecimal sum = parts.stream().map(Money::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    assertEquals(new BigDecimal("10.00"), sum);
  }

  @Test
  void applyPercentage19On100() {
    Money result = MoneyCalculator.applyPercentage(Money.of("100.00", COP), new BigDecimal("19"));
    assertEquals(Money.of("19.00", COP), result);
  }

  @Test
  void applyPercentage0() {
    Money result = MoneyCalculator.applyPercentage(Money.of("100.00", COP), BigDecimal.ZERO);
    assertEquals(Money.of("0.00", COP), result);
  }

  @Test
  void laborCost90minAt10ph() {
    Money costPerHour = Money.of("10.00", COP);
    Money result = MoneyCalculator.laborCost(90, costPerHour);
    assertEquals(Money.of("15.00", COP), result);
  }

  @Test
  void laborCost0min() {
    Money costPerHour = Money.of("10.00", COP);
    Money result = MoneyCalculator.laborCost(0, costPerHour);
    assertEquals(Money.of("0.00", COP), result);
  }

  @Test
  void laborCost60minAt15ph() {
    Money costPerHour = Money.of("15.00", COP);
    Money result = MoneyCalculator.laborCost(60, costPerHour);
    assertEquals(Money.of("15.00", COP), result);
  }
}
