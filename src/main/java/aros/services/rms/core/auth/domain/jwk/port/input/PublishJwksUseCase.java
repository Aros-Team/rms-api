/* (C) 2026 */

package aros.services.rms.core.auth.domain.jwk.port.input;

import aros.services.rms.core.auth.domain.jwk.JwksDocument;

/** Use case for publishing the JWKS document with public keys. */
public interface PublishJwksUseCase {
  /** Returns the current JWKS document containing all published public keys. */
  JwksDocument publish();
}
