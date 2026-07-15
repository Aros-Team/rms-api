/* (C) 2026 */

package aros.services.rms.core.common.money;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.common.money.domain.exception.CurrencyMismatchException;
import aros.services.rms.core.common.money.domain.exception.DivisionByZeroMoneyException;
import aros.services.rms.core.common.money.domain.exception.InvalidMoneyScaleException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.Test;

class MoneyTest {

  private static final Currency COP = Currency.getInstance("COP");
  private static final Currency USD = Currency.getInstance("USD");

  @Test
  void shouldNormalizeScaleOnConstruction() {
    Money m = new Money(new BigDecimal("10.5000"), COP);
    assertEquals(new BigDecimal("10.50"), m.amount());
  }

  @Test
  void shouldAcceptZeroFractionCurrency() {
    Currency jpy = Currency.getInstance("JPY");
    Money m = new Money(new BigDecimal("100"), jpy);
    assertEquals(new BigDecimal("100"), m.amount());
  }

  @Test
  void shouldThrowWhenScaleExceeds10() {
    BigDecimal bad = new BigDecimal("1.12345678901");
    assertThrows(InvalidMoneyScaleException.class, () -> new Money(bad, COP));
  }

  @Test
  void shouldThrowOnNullAmount() {
    assertThrows(NullPointerException.class, () -> new Money(null, COP));
  }

  @Test
  void shouldThrowOnNullCurrency() {
    assertThrows(NullPointerException.class, () -> new Money(BigDecimal.TEN, null));
  }

  @Test
  void zeroFactoryCreatesZero() {
    Money z = Money.zero(COP);
    assertTrue(z.isZero());
    assertEquals(COP, z.currency());
  }

  @Test
  void ofStringFactoryWorks() {
    Money m = Money.of("5.99", COP);
    assertEquals(new BigDecimal("5.99"), m.amount());
  }

  @Test
  void ofLongFactoryWorks() {
    Money m = Money.of(100L, COP);
    assertEquals(new BigDecimal("100.00"), m.amount());
  }

  @Test
  void isPositiveReturnsTrueForPositive() {
    assertTrue(Money.of("10", COP).isPositive());
  }

  @Test
  void isPositiveReturnsFalseForZero() {
    assertFalse(Money.zero(COP).isPositive());
  }

  @Test
  void isNegativeReturnsTrueForNegative() {
    assertTrue(Money.of("-10", COP).isNegative());
  }

  @Test
  void isZeroReturnsTrueForZero() {
    assertTrue(Money.zero(COP).isZero());
  }

  @Test
  void plusSameCurrencyWorks() {
    Money a = Money.of("10.00", COP);
    Money b = Money.of("5.50", COP);
    assertEquals(Money.of("15.50", COP), a.plus(b));
  }

  @Test
  void plusDifferentCurrencyThrows() {
    Money a = Money.of("10.00", COP);
    Money b = Money.of("5.00", USD);
    assertThrows(CurrencyMismatchException.class, () -> a.plus(b));
  }

  @Test
  void minusSameCurrencyWorks() {
    Money a = Money.of("10.00", COP);
    Money b = Money.of("3.00", COP);
    assertEquals(Money.of("7.00", COP), a.minus(b));
  }

  @Test
  void minusDifferentCurrencyThrows() {
    Money a = Money.of("10.00", COP);
    Money b = Money.of("3.00", USD);
    assertThrows(CurrencyMismatchException.class, () -> a.minus(b));
  }

  @Test
  void timesMultipliesCorrectly() {
    Money a = Money.of("10.00", COP);
    Money result = a.times(new BigDecimal("3"));
    assertEquals(Money.of("30.00", COP), result);
  }

  @Test
  void timesWithRoundingModeWorks() {
    Money a = Money.of("10.00", COP);
    Money result = a.times(new BigDecimal("0.3333"), RoundingMode.DOWN);
    assertEquals(Money.of("3.33", COP), result);
  }

  @Test
  void divideHappyPath() {
    Money a = Money.of("10.00", COP);
    Money result = a.divide(new BigDecimal("3"), 2, RoundingMode.HALF_UP);
    assertEquals(Money.of("3.33", COP), result);
  }

  @Test
  void divideByZeroThrows() {
    Money a = Money.of("10.00", COP);
    assertThrows(
        DivisionByZeroMoneyException.class,
        () -> a.divide(BigDecimal.ZERO, 2, RoundingMode.HALF_UP));
  }

  @Test
  void negatedWorks() {
    Money a = Money.of("10.00", COP);
    assertEquals(Money.of("-10.00", COP), a.negated());
  }

  @Test
  void absWorksOnNegative() {
    Money a = Money.of("-10.00", COP);
    assertEquals(Money.of("10.00", COP), a.abs());
  }

  @Test
  void absWorksOnPositive() {
    Money a = Money.of("10.00", COP);
    assertEquals(Money.of("10.00", COP), a.abs());
  }

  @Test
  void percent19On100() {
    Money a = Money.of("100.00", COP);
    Money result = a.percent(new BigDecimal("19"));
    assertEquals(Money.of("19.00", COP), result);
  }

  @Test
  void percent10On50() {
    Money a = Money.of("50.00", COP);
    Money result = a.percent(new BigDecimal("10"));
    assertEquals(Money.of("5.00", COP), result);
  }

  @Test
  void applyMargin20On100() {
    Money a = Money.of("100.00", COP);
    Money result = a.applyMargin(new BigDecimal("20"));
    assertEquals(Money.of("120.00", COP), result);
  }

  @Test
  void allocate3PartsFrom10() {
    Money ten = Money.of("10.00", COP);
    List<Money> parts = ten.allocate(3);
    assertEquals(3, parts.size());
    assertEquals(Money.of("3.34", COP), parts.get(0));
    assertEquals(Money.of("3.33", COP), parts.get(1));
    assertEquals(Money.of("3.33", COP), parts.get(2));
    BigDecimal sum = parts.stream().map(Money::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    assertEquals(new BigDecimal("10.00"), sum);
  }

  @Test
  void allocate2PartsFrom1() {
    Money one = Money.of("1.00", COP);
    List<Money> parts = one.allocate(2);
    assertEquals(2, parts.size());
    BigDecimal sum = parts.stream().map(Money::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    assertEquals(new BigDecimal("1.00"), sum);
  }

  @Test
  void allocateWithRatios() {
    Money ten = Money.of("10.00", COP);
    List<Money> parts = ten.allocate(List.of(new BigDecimal("1"), new BigDecimal("2")));
    assertEquals(2, parts.size());
    BigDecimal sum = parts.stream().map(Money::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    assertEquals(new BigDecimal("10.00"), sum);
  }

  @Test
  void compareToWorks() {
    Money a = Money.of("10.00", COP);
    Money b = Money.of("5.00", COP);
    assertTrue(a.compareTo(b) > 0);
    assertTrue(b.compareTo(a) < 0);
    assertEquals(0, a.compareTo(Money.of("10.00", COP)));
  }

  @Test
  void isGreaterThanWorks() {
    Money a = Money.of("10.00", COP);
    Money b = Money.of("5.00", COP);
    assertTrue(a.isGreaterThan(b));
    assertFalse(b.isGreaterThan(a));
  }

  @Test
  void isGreaterOrEqualWorks() {
    Money a = Money.of("10.00", COP);
    Money b = Money.of("10.00", COP);
    Money c = Money.of("5.00", COP);
    assertTrue(a.isGreaterOrEqual(b));
    assertTrue(a.isGreaterOrEqual(c));
    assertFalse(c.isGreaterOrEqual(a));
  }

  @Test
  void isLessThanWorks() {
    Money a = Money.of("10.00", COP);
    Money b = Money.of("5.00", COP);
    assertTrue(b.isLessThan(a));
    assertFalse(a.isLessThan(b));
  }

  @Test
  void compareToDifferentCurrencyThrows() {
    Money a = Money.of("10.00", COP);
    Money b = Money.of("5.00", USD);
    assertThrows(CurrencyMismatchException.class, () -> a.compareTo(b));
  }

  @Test
  void toStringReturnsFormatted() {
    Money a = Money.of("1.20", COP);
    assertEquals("COP 1.20", a.toString());
  }

  @Test
  void toStringForJpy() {
    Currency jpy = Currency.getInstance("JPY");
    Money a = Money.of("500", jpy);
    assertEquals("JPY 500", a.toString());
  }

  @Test
  void equalityWorks() {
    Money a = Money.of("10.00", COP);
    Money b = Money.of("10.00", COP);
    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
  }

  @Test
  void nullAmountInOfStringThrows() {
    assertThrows(NullPointerException.class, () -> Money.of((String) null, COP));
  }
}
