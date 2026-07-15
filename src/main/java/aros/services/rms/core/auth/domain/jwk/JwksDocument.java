/* (C) 2026 */

package aros.services.rms.core.auth.domain.jwk;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Immutable document containing a set of JWK keys. */
public record JwksDocument(List<JwkKey> keys) {
  /** Validates keys is non-null and wraps with an unmodifiable list. */
  public JwksDocument {
    Objects.requireNonNull(keys, "keys must not be null");
    keys = List.copyOf(keys);
  }

  /** Finds a key by its key ID. */
  public Optional<JwkKey> findById(JwkKeyId kid) {
    return keys.stream().filter(k -> k.kid().equals(kid)).findFirst();
  }
}
