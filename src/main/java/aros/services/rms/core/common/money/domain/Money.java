/* (C) 2026 */

package aros.services.rms.core.common.money.domain;

import aros.services.rms.core.common.money.domain.exception.CurrencyMismatchException;
import aros.services.rms.core.common.money.domain.exception.DivisionByZeroMoneyException;
import aros.services.rms.core.common.money.domain.exception.InvalidMoneyScaleException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

/** Immutable value object representing a monetary amount in a specific currency. */
public record Money(BigDecimal amount, Currency currency) {

  /**
   * Canonical constructor with validation. Ensures non-null, scale &le; 10, and normalizes to the
   * currency's default fraction digits using {@link RoundingMode#HALF_UP}.
   *
   * @param amount the monetary amount
   * @param currency the ISO 4217 currency
   * @throws NullPointerException if amount or currency is null
   * @throws InvalidMoneyScaleException if scale exceeds 10
   */
  public Money {
    Objects.requireNonNull(amount, "amount");
    Objects.requireNonNull(currency, "currency");
    if (amount.scale() > 10) {
      throw new InvalidMoneyScaleException(amount.scale());
    }
    amount = amount.setScale(currency.getDefaultFractionDigits(), RoundingMode.HALF_UP);
  }

  /**
   * Creates a zero amount for the given currency.
   *
   * @param currency the currency
   * @return a Money representing zero
   */
  public static Money zero(Currency currency) {
    return new Money(BigDecimal.ZERO, currency);
  }

  /**
   * Creates a Money from a string amount and currency.
   *
   * @param amount the amount as a string (e.g. "10.50")
   * @param currency the currency
   * @return a new Money instance
   */
  public static Money of(String amount, Currency currency) {
    return new Money(new BigDecimal(amount), currency);
  }

  /**
   * Creates a Money from a long amount and currency.
   *
   * @param amount the amount as a long (in minor units implied by the string representation)
   * @param currency the currency
   * @return a new Money instance
   */
  public static Money of(long amount, Currency currency) {
    return new Money(BigDecimal.valueOf(amount), currency);
  }

  /**
   * Returns the raw BigDecimal amount. Exposed for persistence mapping only.
   *
   * @return the amount
   */
  @Override
  public BigDecimal amount() {
    return amount;
  }

  /**
   * Returns whether this amount is positive.
   *
   * @return true if greater than zero
   */
  public boolean isPositive() {
    return amount.signum() > 0;
  }

  /**
   * Returns whether this amount is zero.
   *
   * @return true if zero
   */
  public boolean isZero() {
    return amount.signum() == 0;
  }

  /**
   * Returns whether this amount is negative.
   *
   * @return true if less than zero
   */
  public boolean isNegative() {
    return amount.signum() < 0;
  }

  /**
   * Adds another Money of the same currency.
   *
   * @param other the money to add
   * @return the sum
   * @throws CurrencyMismatchException if currencies differ
   */
  public Money plus(Money other) {
    assertSameCurrency(other);
    return new Money(amount.add(other.amount), currency);
  }

  /**
   * Subtracts another Money of the same currency.
   *
   * @param other the money to subtract
   * @return the difference
   * @throws CurrencyMismatchException if currencies differ
   */
  public Money minus(Money other) {
    assertSameCurrency(other);
    return new Money(amount.subtract(other.amount), currency);
  }

  /**
   * Multiplies by a scalar BigDecimal.
   *
   * @param multiplier the multiplier
   * @return the product
   */
  public Money times(BigDecimal multiplier) {
    Objects.requireNonNull(multiplier, "multiplier");
    return new Money(amount.multiply(multiplier), currency);
  }

  /**
   * Multiplies by a scalar BigDecimal with a specific rounding mode.
   *
   * @param multiplier the multiplier
   * @param mode the rounding mode
   * @return the product rounded to the currency's default fraction digits
   */
  public Money times(BigDecimal multiplier, RoundingMode mode) {
    Objects.requireNonNull(multiplier, "multiplier");
    Objects.requireNonNull(mode, "mode");
    BigDecimal result =
        amount.multiply(multiplier).setScale(currency.getDefaultFractionDigits(), mode);
    return new Money(result, currency);
  }

  /**
   * Divides by a scalar BigDecimal with explicit scale and rounding mode.
   *
   * @param divisor the divisor
   * @param scale the scale of the result
   * @param mode the rounding mode
   * @return the quotient
   * @throws DivisionByZeroMoneyException if divisor is zero
   */
  public Money divide(BigDecimal divisor, int scale, RoundingMode mode) {
    Objects.requireNonNull(divisor, "divisor");
    Objects.requireNonNull(mode, "mode");
    if (divisor.signum() == 0) {
      throw new DivisionByZeroMoneyException(currency);
    }
    return new Money(amount.divide(divisor, scale, mode), currency);
  }

  /**
   * Returns the negated amount of this Money.
   *
   * @return the negated value
   */
  public Money negated() {
    return new Money(amount.negate(), currency);
  }

  /**
   * Returns the absolute value of this Money.
   *
   * @return the absolute value
   */
  public Money abs() {
    return new Money(amount.abs(), currency);
  }

  /**
   * Calculates a percentage of this amount.
   *
   * @param pct the percentage (e.g. 19 for 19%)
   * @return the computed percentage amount
   */
  public Money percent(BigDecimal pct) {
    Objects.requireNonNull(pct, "pct");
    BigDecimal fraction = pct.divide(new BigDecimal("100"), 8, RoundingMode.HALF_UP);
    BigDecimal result =
        amount
            .multiply(fraction)
            .setScale(currency.getDefaultFractionDigits(), RoundingMode.HALF_UP);
    return new Money(result, currency);
  }

  /**
   * Applies a margin percentage over this amount (this + percent).
   *
   * @param marginPct the margin percentage
   * @return the resulting amount
   */
  public Money applyMargin(BigDecimal marginPct) {
    return plus(percent(marginPct));
  }

  /**
   * Splits this amount into equal parts using the largest-remainder method. Ensures the sum of
   * parts equals the original amount.
   *
   * @param parts the number of parts
   * @return a list of allocated amounts
   * @throws IllegalArgumentException if parts is not positive
   */
  public List<Money> allocate(int parts) {
    if (parts <= 0) {
      throw new IllegalArgumentException("parts must be positive, was: " + parts);
    }
    int defaultFractionDigits = currency.getDefaultFractionDigits();
    BigDecimal low =
        amount.divide(BigDecimal.valueOf(parts), defaultFractionDigits, RoundingMode.HALF_UP);
    BigDecimal remainder = amount.subtract(low.multiply(BigDecimal.valueOf(parts)));
    int remainderCents = remainder.movePointRight(defaultFractionDigits).intValue();
    List<Money> result = new ArrayList<>(parts);
    for (int i = 0; i < parts; i++) {
      Money part = new Money(low, currency);
      if (i < remainderCents) {
        part = part.plus(new Money(BigDecimal.ONE.movePointLeft(defaultFractionDigits), currency));
      }
      result.add(part);
    }
    return result;
  }

  /**
   * Splits this amount according to the given ratios using weighted allocation.
   *
   * @param ratios the allocation ratios
   * @return a list of allocated amounts
   * @throws IllegalArgumentException if ratios is empty
   */
  public List<Money> allocate(List<BigDecimal> ratios) {
    Objects.requireNonNull(ratios, "ratios");
    if (ratios.isEmpty()) {
      throw new IllegalArgumentException("ratios must not be empty");
    }
    BigDecimal totalRatio = BigDecimal.ZERO;
    for (BigDecimal r : ratios) {
      totalRatio = totalRatio.add(r);
    }
    int defaultFractionDigits = currency.getDefaultFractionDigits();
    List<Money> results = new ArrayList<>(ratios.size());
    BigDecimal allocated = BigDecimal.ZERO;
    for (int i = 0; i < ratios.size(); i++) {
      BigDecimal ratio = ratios.get(i);
      if (i == ratios.size() - 1) {
        results.add(new Money(amount.subtract(allocated), currency));
      } else {
        BigDecimal part =
            amount.multiply(ratio).divide(totalRatio, defaultFractionDigits, RoundingMode.HALF_UP);
        allocated = allocated.add(part);
        results.add(new Money(part, currency));
      }
    }
    return results;
  }

  /**
   * Returns whether this amount is greater than another of the same currency.
   *
   * @param other the money to compare against
   * @return true if greater
   * @throws CurrencyMismatchException if currencies differ
   */
  public boolean isGreaterThan(Money other) {
    assertSameCurrency(other);
    return amount.compareTo(other.amount) > 0;
  }

  /**
   * Returns whether this amount is greater than or equal to another of the same currency.
   *
   * @param other the money to compare against
   * @return true if greater or equal
   * @throws CurrencyMismatchException if currencies differ
   */
  public boolean isGreaterOrEqual(Money other) {
    assertSameCurrency(other);
    return amount.compareTo(other.amount) >= 0;
  }

  /**
   * Returns whether this amount is less than another of the same currency.
   *
   * @param other the money to compare against
   * @return true if less
   * @throws CurrencyMismatchException if currencies differ
   */
  public boolean isLessThan(Money other) {
    assertSameCurrency(other);
    return amount.compareTo(other.amount) < 0;
  }

  /**
   * Compares this amount to another of the same currency.
   *
   * @param other the money to compare against
   * @return negative, zero, or positive
   * @throws CurrencyMismatchException if currencies differ
   */
  public int compareTo(Money other) {
    assertSameCurrency(other);
    return amount.compareTo(other.amount);
  }

  private void assertSameCurrency(Money other) {
    if (other == null || !currency.equals(other.currency)) {
      throw new CurrencyMismatchException(currency, other == null ? null : other.currency);
    }
  }

  /**
   * Returns a string in the format {@code "COP 1.20"}.
   *
   * @return formatted string
   */
  @Override
  public String toString() {
    return currency.getCurrencyCode() + " " + amount.toPlainString();
  }
}
