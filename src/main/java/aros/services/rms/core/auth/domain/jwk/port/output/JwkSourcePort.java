/* (C) 2026 */

package aros.services.rms.core.auth.domain.jwk.port.output;

import aros.services.rms.core.auth.domain.jwk.JwkKey;
import aros.services.rms.core.auth.domain.jwk.JwksDocument;
import java.util.Optional;

/** Output port for loading JWK key material from the configured source. */
public interface JwkSourcePort {
  /** Loads all available JWK keys. */
  JwksDocument loadAll();

  /** Returns the currently active signing key, if any. */
  Optional<JwkKey> activeSigningKey();
}
