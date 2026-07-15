/* (C) 2026 */

package aros.services.rms.core.auth.domain.jwk;

import java.util.Objects;

/** Value object representing a JWK Key Identifier (kid). */
public record JwkKeyId(String value) {
  /** Validates the key ID is non-null and non-blank. */
  public JwkKeyId {
    Objects.requireNonNull(value, "kid must not be null");
    if (value.isBlank()) {
      throw new IllegalArgumentException("kid must not be blank");
    }
  }
}
