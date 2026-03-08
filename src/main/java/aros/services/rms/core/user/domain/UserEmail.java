/* (C) 2026 */
package aros.services.rms.core.user.domain;

public record UserEmail(String value) {
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
