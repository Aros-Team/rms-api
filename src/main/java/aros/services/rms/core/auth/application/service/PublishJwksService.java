/* (C) 2026 */

package aros.services.rms.core.auth.application.service;

import aros.services.rms.core.auth.domain.jwk.JwksDocument;
import aros.services.rms.core.auth.domain.jwk.exception.NoActiveSigningKeyException;
import aros.services.rms.core.auth.domain.jwk.port.input.PublishJwksUseCase;
import aros.services.rms.core.auth.domain.jwk.port.output.JwkSourcePort;

/** Application service that publishes the JWKS document. */
public class PublishJwksService implements PublishJwksUseCase {
  private final JwkSourcePort jwkSourcePort;

  /** Creates a new PublishJwksService with the given source port. */
  public PublishJwksService(JwkSourcePort jwkSourcePort) {
    this.jwkSourcePort = jwkSourcePort;
  }

  @Override
  public JwksDocument publish() {
    JwksDocument doc = jwkSourcePort.loadAll();
    if (doc.keys().isEmpty()) {
      throw new NoActiveSigningKeyException();
    }
    return doc;
  }
}
