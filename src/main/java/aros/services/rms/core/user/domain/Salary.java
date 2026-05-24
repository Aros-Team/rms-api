/* (C) 2026 */

package aros.services.rms.core.user.domain;

import java.math.BigDecimal;

/** Value object representing a salary amount. */
public record Salary(BigDecimal value) implements Comparable<Salary> {

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
    if (value.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("El salario debe ser un valor positivo");
    }
  }

  /**
   * Factory method to create a Salary from a BigDecimal.
   *
   * @param value the salary amount
   * @return new Salary instance
   */
  public static Salary of(BigDecimal value) {
    return new Salary(value);
  }

  /**
   * Checks if this salary is greater than another.
   *
   * @param other the other salary
   * @return true if this salary is greater
   */
  public boolean isGreaterThan(Salary other) {
    return this.value.compareTo(other.value) > 0;
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
