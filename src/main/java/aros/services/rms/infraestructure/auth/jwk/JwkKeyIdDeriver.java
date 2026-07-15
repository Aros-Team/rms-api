/* (C) 2026 */

package aros.services.rms.infraestructure.auth.jwk;

import aros.services.rms.core.auth.domain.jwk.JwkKeyId;
import java.security.MessageDigest;
import java.security.interfaces.RSAPublicKey;
import java.util.HexFormat;

/** Deterministic JWK Key ID derivation from an RSA public key. */
public final class JwkKeyIdDeriver {

  private JwkKeyIdDeriver() {}

  /**
   * Derives a deterministic key ID from an RSA public key using SHA-256 thumbprint.
   *
   * @param publicKey the RSA public key
   * @return a JwkKeyId with "k-" prefix and first 16 hex chars of the SHA-256 digest
   */
  public static JwkKeyId from(RSAPublicKey publicKey) {
    byte[] digest = sha256(publicKey.getEncoded());
    String hex = HexFormat.of().formatHex(digest).substring(0, 16);
    return new JwkKeyId("k-" + hex);
  }

  private static byte[] sha256(byte[] input) {
    try {
      return MessageDigest.getInstance("SHA-256").digest(input);
    } catch (Exception e) {
      throw new RuntimeException("SHA-256 not available", e);
    }
  }
}
