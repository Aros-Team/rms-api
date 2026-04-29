/* (C) 2026 */

package aros.services.rms.core.user.domain;

/** Value object representing the unique identifier for a user. */
public record UserId(Long value) {
  /**
   * Canonical constructor with validation.
   *
   * @param value the user identifier value
   * @throws IllegalArgumentException if value is null
   */
  public UserId {
    if (value == null) {
      throw new IllegalArgumentException("El ID del usuario no puede ser nulo");
    }
  }

  /**
   * Factory method to create a UserId from a Long value.
   *
   * @param value the user identifier value
   * @return new UserId instance
   */
  public static UserId of(Long value) {
    return new UserId(value);
  }
}
