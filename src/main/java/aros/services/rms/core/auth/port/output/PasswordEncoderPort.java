/* (C) 2026 */

package aros.services.rms.core.auth.port.output;

/** Output port for password encoding and validation operations. */
public interface PasswordEncoderPort {
  /**
   * Encodes a raw password.
   *
   * @param password the raw password to encode
   * @return the encoded password string
   */
  String encode(String password);

  /**
   * Validates a password against an encoded string.
   *
   * @param password the raw password to validate
   * @param encoded the encoded password to check against
   * @return true if the password matches
   */
  Boolean validate(String password, String encoded);
}
