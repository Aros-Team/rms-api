/* (C) 2026 */
package aros.services.rms.core.user.port.output;

import aros.services.rms.core.user.domain.User;
import java.util.Optional;

public interface UserRepositoryPort {
  public Optional<User> findByEmail(String email);
}
