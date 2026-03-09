/* (C) 2026 */
package aros.services.rms.infraestructure.user.persistence.jpa;

import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Transactional
public class UserRepositoryAdapter implements UserRepositoryPort {
  @Autowired private JpaUserRepository internal;

  @Autowired private UserPersistenceMapper userMapper;

  @Override
  public Optional<User> findByEmail(String email) {
    return this.internal.findByEmail(email).map((u) -> userMapper.toDomain(u));
  }

  @Override
  public Optional<User> findById(UserId id) {
    return this.internal.findById(id.value()).map((u) -> userMapper.toDomain(u));
  }

  @Override
  public User save(User user) {
    return userMapper.toDomain(this.internal.save(userMapper.toEntity(user)));
  }

  @Override
  public boolean existsByEmailOrDocument(String document, String email) {
    return this.internal.existsByDocumentOrEmail(document, email);
  }
}
