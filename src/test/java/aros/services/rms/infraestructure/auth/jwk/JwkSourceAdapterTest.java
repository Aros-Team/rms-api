/* (C) 2026 */

package aros.services.rms.infraestructure.auth.jwk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import aros.services.rms.core.auth.domain.jwk.JwkKey;
import aros.services.rms.core.auth.domain.jwk.JwkKeyId;
import aros.services.rms.core.auth.domain.jwk.JwksDocument;
import aros.services.rms.infraestructure.common.config.JwtConfigValidator;
import aros.services.rms.infraestructure.common.config.JwtConfigValidator.JwkKeyMaterial;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class JwkSourceAdapterTest {

  @Test
  void should_load_single_key() {
    JwtConfigValidator validator = mock(JwtConfigValidator.class);
    JwkSourceAdapter adapter = new JwkSourceAdapter(validator);

    KeyPair keyPair = generateRsaKeyPair();
    RSAPublicKey pub = (RSAPublicKey) keyPair.getPublic();
    RSAPrivateKey priv = (RSAPrivateKey) keyPair.getPrivate();
    JwkKeyId kid = new JwkKeyId("k-test000000000001");

    when(validator.allKeys()).thenReturn(List.of(new JwkKeyMaterial(kid, pub, priv)));
    when(validator.activeKeyId()).thenReturn(kid);

    JwksDocument doc = adapter.loadAll();

    assertThat(doc.keys()).hasSize(1);
    JwkKey key = doc.keys().get(0);
    assertThat(key.kid()).isEqualTo(kid);
    assertThat(key.hasPrivate()).isTrue();
  }

  @Test
  void activeSigningKey_should_return_active_key() {
    JwtConfigValidator validator = mock(JwtConfigValidator.class);
    JwkSourceAdapter adapter = new JwkSourceAdapter(validator);

    KeyPair keyPair = generateRsaKeyPair();
    RSAPublicKey pub = (RSAPublicKey) keyPair.getPublic();
    RSAPrivateKey priv = (RSAPrivateKey) keyPair.getPrivate();
    JwkKeyId kid = new JwkKeyId("k-active000000001");

    when(validator.allKeys()).thenReturn(List.of(new JwkKeyMaterial(kid, pub, priv)));
    when(validator.activeKeyId()).thenReturn(kid);

    Optional<JwkKey> active = adapter.activeSigningKey();

    assertThat(active).isPresent();
    assertThat(active.get().kid()).isEqualTo(kid);
  }

  private static KeyPair generateRsaKeyPair() {
    try {
      KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
      keyGen.initialize(2048);
      return keyGen.generateKeyPair();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
