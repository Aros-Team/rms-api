package aros.services.rms.core.auth.domain.jwk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class JwkKeyIdTest {

  @Test
  void shouldRejectNull() {
    assertThrows(NullPointerException.class, () -> new JwkKeyId(null));
  }

  @Test
  void shouldRejectBlank() {
    assertThrows(IllegalArgumentException.class, () -> new JwkKeyId("  "));
  }

  @Test
  void shouldAcceptValidValue() {
    JwkKeyId kid = new JwkKeyId("my-key-id");
    assertEquals("my-key-id", kid.value());
  }
}
