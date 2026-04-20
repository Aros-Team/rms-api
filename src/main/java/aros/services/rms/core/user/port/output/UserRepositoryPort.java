/* (C) 2026 */
package aros.services.rms.core.user.port.output;

import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.core.user.domain.UserStatus;
import aros.services.rms.core.user.domain.UserWithAreas;
import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {
  Optional<User> findByEmail(String email);

  Optional<UserWithAreas> findByEmailWithAreas(String email);

  Optional<User> findById(UserId id);

  boolean existsByEmailOrDocument(String document, String email);

  List<User> findAll();

  List<User> findByStatus(UserStatus status);

  User save(User user);
}
