/* (C) 2026 */
package aros.services.rms.core.email.port.output;

import aros.services.rms.core.email.domain.Email;

public interface EmailServicePort {
  void send(Email email);
}
