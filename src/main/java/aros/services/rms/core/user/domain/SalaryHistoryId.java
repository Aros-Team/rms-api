/* (C) 2026 */

package aros.services.rms.core.user.domain;

/** Value object representing the unique identifier for a salary history entry. */
public record SalaryHistoryId(Long value) {

  /**
   * Canonical constructor with validation.
   *
   * @param value the identifier value
   * @throws IllegalArgumentException if value is null
   */
  public SalaryHistoryId {
    if (value == null) {
      throw new IllegalArgumentException("El ID del historial salarial no puede ser nulo");
    }
  }

  /**
   * Factory method to create a SalaryHistoryId from a Long.
   *
   * @param value the identifier value
   * @return new SalaryHistoryId instance
   */
  public static SalaryHistoryId of(Long value) {
    return new SalaryHistoryId(value);
  }
}
