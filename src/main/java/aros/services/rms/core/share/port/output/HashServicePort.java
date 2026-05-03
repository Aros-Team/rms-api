/* (C) 2026 */

package aros.services.rms.core.share.port.output;

/** Output port for hashing operations (used for tokens and passwords). */
public interface HashServicePort {
  /**
   * Hashes an input string.
   *
   * @param input the string to hash
   * @return the hashed string
   */
  String hash(String input);
}
