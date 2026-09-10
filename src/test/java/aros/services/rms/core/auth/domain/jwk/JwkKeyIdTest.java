package aros.services.rms.core.auth.domain.jwk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link JwkKeyId}.
 *
 * <p><b>Feature:</b> JWK key ID derivation from RSA public keys
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>should Reject Null
 *   <li>should Reject Blank
 *   <li>should Accept Valid Value
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Verifies business logic with unit-level assertions
 * </ul>
 */
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
