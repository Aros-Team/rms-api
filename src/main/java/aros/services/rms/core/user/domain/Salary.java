/* (C) 2026 */

package aros.services.rms.core.user.domain;

import aros.services.rms.core.common.money.domain.Money;
import java.math.BigDecimal;
import java.util.Currency;

/** Value object representing a salary amount. */
public record Salary(Money value) implements Comparable<Salary> {

  /**
   * Canonical constructor with validation.
   *
   * @param value the salary amount
   * @throws IllegalArgumentException if value is null or not positive
   */
  public Salary {
    if (value == null) {
      throw new IllegalArgumentException("El salario no puede ser nulo");
    }
    if (value.amount().compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("El salario debe ser un valor positivo");
    }
  }

  /**
   * Factory method to create a Salary from a Money.
   *
   * @param value the salary amount
   * @return new Salary instance
   */
  public static Salary of(Money value) {
    return new Salary(value);
  }

  /**
   * Factory method to create a Salary from a BigDecimal and Currency.
   *
   * @param amount the salary amount
   * @param currency the currency
   * @return new Salary instance
   */
  public static Salary of(BigDecimal amount, Currency currency) {
    return new Salary(new Money(amount, currency));
  }

  /**
   * Factory method to create a Salary from a BigDecimal (COP).
   *
   * @param amount the salary amount
   * @return new Salary instance
   */
  public static Salary of(BigDecimal amount) {
    return new Salary(new Money(amount, Currency.getInstance("COP")));
  }

  /**
   * Checks if this salary is greater than another.
   *
   * @param other the other salary
   * @return true if this salary is greater
   */
  public boolean isGreaterThan(Salary other) {
    return this.value.isGreaterThan(other.value);
  }

  /**
   * Checks if this salary is equal to another.
   *
   * @param other the other salary
   * @return true if equal
   */
  public boolean isEqualTo(Salary other) {
    return this.value.compareTo(other.value) == 0;
  }

  @Override
  public int compareTo(Salary other) {
    return this.value.compareTo(other.value);
  }
}
