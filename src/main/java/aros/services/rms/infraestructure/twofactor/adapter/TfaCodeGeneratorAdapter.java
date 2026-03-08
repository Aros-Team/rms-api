/* (C) 2026 */
package aros.services.rms.infraestructure.twofactor.adapter;

import aros.services.rms.core.twofactor.port.output.TfaCodeGeneratorPort;
import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class TfaCodeGeneratorAdapter implements TfaCodeGeneratorPort {

  private final SecureRandom random = new SecureRandom();

  @Override
  public String generateCode(int digits) {
    StringBuilder code = new StringBuilder();
    for (int i = 0; i < digits; i++) {
      code.append(random.nextInt(10));
    }
    return code.toString();
  }
}
