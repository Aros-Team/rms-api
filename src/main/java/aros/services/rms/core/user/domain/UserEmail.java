/* (C) 2026 */

package aros.services.rms.core.user.domain;

/**
 * Value object representing a user's email address. Validates format and ensures non-null/non-blank
 * values.
 */
public record UserEmail(String value) {
  /**
   * Canonical constructor with validation.
   *
   * @param value the email address value
   * @throws IllegalArgumentException if value is null, blank, or invalid format
   */
  public UserEmail {
    // Validación de reglas de negocio
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("El email no puede estar vacío");
    }

    if (!value.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
      throw new IllegalArgumentException("Formato de email inválido");
    }

    // Opcional: Normalización
    value = value.toLowerCase().trim();
  }
}
