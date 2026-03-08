/* (C) 2026 */
package aros.services.rms.core.twofactor.port.output;

public interface TfaCodeGeneratorPort {
  String generateCode(int digits);
}
