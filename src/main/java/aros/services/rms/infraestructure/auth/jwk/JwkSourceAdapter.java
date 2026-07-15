/* (C) 2026 */

package aros.services.rms.infraestructure.auth.jwk;

import aros.services.rms.core.auth.domain.jwk.JwkAlgorithm;
import aros.services.rms.core.auth.domain.jwk.JwkKey;
import aros.services.rms.core.auth.domain.jwk.JwkKeyId;
import aros.services.rms.core.auth.domain.jwk.JwkUse;
import aros.services.rms.core.auth.domain.jwk.JwksDocument;
import aros.services.rms.core.auth.domain.jwk.exception.InvalidJwkConfigurationException;
import aros.services.rms.core.auth.domain.jwk.port.output.JwkSourcePort;
import aros.services.rms.infraestructure.common.config.JwtConfigValidator;
import aros.services.rms.infraestructure.common.config.JwtConfigValidator.JwkKeyMaterial;
import com.nimbusds.jose.jwk.RSAKey;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Adapter that loads JWK key material from the JWT configuration validator. */
@Component
public class JwkSourceAdapter implements JwkSourcePort {

  private final JwtConfigValidator validator;

  /** Creates a new JwkSourceAdapter with the given validator. */
  public JwkSourceAdapter(JwtConfigValidator validator) {
    this.validator = validator;
  }

  @Override
  public JwksDocument loadAll() {
    List<JwkKey> domainKeys = validator.allKeys().stream().map(this::toDomainJwkKey).toList();
    return new JwksDocument(domainKeys);
  }

  @Override
  public Optional<JwkKey> activeSigningKey() {
    JwkKeyId activeKid = validator.activeKeyId();
    return loadAll().findById(activeKid);
  }

  private JwkKey toDomainJwkKey(JwkKeyMaterial m) {
    try {
      RSAKey nimbus =
          m.privateKey() != null
              ? new RSAKey.Builder(m.publicKey()).privateKey(m.privateKey()).build()
              : new RSAKey.Builder(m.publicKey()).build();
      nimbus = new RSAKey.Builder(nimbus).keyID(m.kid().value()).build();
      return new JwkKey(
          m.kid(), JwkAlgorithm.RS256, JwkUse.SIGNATURE, nimbus, m.privateKey() != null);
    } catch (RuntimeException e) {
      throw new InvalidJwkConfigurationException("Failed to build JWK for kid=" + m.kid(), e);
    }
  }
}
