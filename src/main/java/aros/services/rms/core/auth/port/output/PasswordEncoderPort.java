/* (C) 2026 */
package aros.services.rms.core.auth.port.output;

public interface PasswordEncoderPort {
  String encode(String password);

  Boolean validate(String password, String encoded);
}
