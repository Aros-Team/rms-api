/* (C) 2026 */

package aros.services.rms.infraestructure.auth.bcrypt;

import aros.services.rms.core.auth.port.output.PasswordEncoderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/** Adapter for BCrypt password encoding. */
@Service
@RequiredArgsConstructor
public class BcryptPasswordEncoderAdapter implements PasswordEncoderPort {

  private final BCryptPasswordEncoder passwordEncoder;

  @Override
  public String encode(String password) {
    return passwordEncoder.encode(password);
  }

  @Override
  public Boolean validate(String password, String encoded) {
    return passwordEncoder.matches(password, encoded);
  }
}
