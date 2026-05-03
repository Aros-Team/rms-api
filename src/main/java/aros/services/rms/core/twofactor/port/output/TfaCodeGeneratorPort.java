/* (C) 2026 */

package aros.services.rms.core.twofactor.port.output;

/** Output port for generating two-factor authentication codes. */
public interface TfaCodeGeneratorPort {
  /**
   * Generates a numeric code.
   *
   * @param digits number of digits for the code
   * @return the generated code as a string
   */
  String generateCode(int digits);
}
