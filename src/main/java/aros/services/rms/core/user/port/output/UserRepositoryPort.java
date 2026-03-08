/* (C) 2026 */
package aros.services.rms.core.user.port.output;

import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserId;

import java.util.Optional;

public interface UserRepositoryPort {
  Optional<User> findByEmail(String email);
  Optional<User> findById(UserId id);
}
