/* (C) 2026 */
package aros.services.rms.core.user.domain;

public record UserId(Long value) {
  public UserId {
    if (value == null) {
      throw new IllegalArgumentException("El ID del usuario no puede ser nulo");
    }
  }

  public static UserId of(Long value) {
    return new UserId(value);
  }
}
