/* (C) 2026 */

package aros.services.rms.infraestructure.auth.jwk;

import static org.assertj.core.api.Assertions.assertThat;

import aros.services.rms.core.auth.domain.jwk.JwkKeyId;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import org.junit.jupiter.api.Test;

class JwkKeyIdDeriverTest {

  @Test
  void should_derive_same_kid_for_same_key() {
    RSAPublicKey pubKey = (RSAPublicKey) generateRsaKeyPair().getPublic();
    JwkKeyId first = JwkKeyIdDeriver.from(pubKey);
    JwkKeyId second = JwkKeyIdDeriver.from(pubKey);
    assertThat(first).isEqualTo(second);
  }

  @Test
  void should_derive_different_kid_for_different_keys() {
    RSAPublicKey keyA = (RSAPublicKey) generateRsaKeyPair().getPublic();
    RSAPublicKey keyB = (RSAPublicKey) generateRsaKeyPair().getPublic();
    JwkKeyId kidA = JwkKeyIdDeriver.from(keyA);
    JwkKeyId kidB = JwkKeyIdDeriver.from(keyB);
    assertThat(kidA).isNotEqualTo(kidB);
  }

  @Test
  void shouldDeriveKidThatStartsWithK() {
    RSAPublicKey pubKey = (RSAPublicKey) generateRsaKeyPair().getPublic();
    JwkKeyId kid = JwkKeyIdDeriver.from(pubKey);
    assertThat(kid.value()).startsWith("k-");
  }

  @Test
  void shouldDerive16HexCharsAfterPrefix() {
    RSAPublicKey pubKey = (RSAPublicKey) generateRsaKeyPair().getPublic();
    JwkKeyId kid = JwkKeyIdDeriver.from(pubKey);
    String hexPart = kid.value().substring(2);
    assertThat(hexPart).hasSize(16);
    assertThat(hexPart).matches("[0-9a-f]+");
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
