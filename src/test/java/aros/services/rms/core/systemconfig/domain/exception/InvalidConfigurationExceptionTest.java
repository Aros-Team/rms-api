/* (C) 2026 */

package aros.services.rms.core.systemconfig.domain.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link InvalidConfigurationException}. */
class InvalidConfigurationExceptionTest {

  // ---------------------------------------------------------------------------
  // UC-01: should_create_exception_with_message
  // ---------------------------------------------------------------------------

  @Test
  void should_create_exception_with_message() {
    InvalidConfigurationException ex =
        new InvalidConfigurationException("default_currency", "nulo o vacío");

    assertEquals(
        "Configuración inválida: clave=default_currency, valor=nulo o vacío", ex.getMessage());
    assertInstanceOf(RuntimeException.class, ex);
  }

  // ---------------------------------------------------------------------------
  // UC-02: should_handle_null_key
  // ---------------------------------------------------------------------------

  @Test
  void should_handle_null_key() {
    InvalidConfigurationException ex = new InvalidConfigurationException(null, "blank");

    assertEquals("Configuración inválida: clave=null, valor=blank", ex.getMessage());
  }
}
