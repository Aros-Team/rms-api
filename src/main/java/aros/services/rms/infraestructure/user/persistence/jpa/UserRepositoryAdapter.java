/* (C) 2026 */

package aros.services.rms.infraestructure.user.persistence.jpa;

import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.core.user.domain.UserRole;
import aros.services.rms.core.user.domain.UserStatus;
import aros.services.rms.core.user.domain.UserWithAreas;
import aros.services.rms.core.user.port.output.AdminRepositoryPort;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/** Adapter for UserRepositoryPort and AdminRepositoryPort. */
@Repository
@RequiredArgsConstructor
@Transactional
public class UserRepositoryAdapter implements UserRepositoryPort, AdminRepositoryPort {
  @Autowired private JpaUserRepository internal;

  @Autowired private UserPersistenceMapper userMapper;

  /** Finds a user by email. */
  @Override
  public Optional<User> findByEmail(String email) {
    return this.internal.findByEmail(email).map((u) -> userMapper.toDomain(u));
  }

  /** Finds a user with areas by email. */
  @Override
  public Optional<UserWithAreas> findByEmailWithAreas(String email) {
    return this.internal.findByEmailWithAreas(email).map(u -> userMapper.toUserWithAreas(u));
  }

  /** Finds a user by ID. */
  @Override
  public Optional<User> findById(UserId id) {
    return this.internal.findById(id.value()).map((u) -> userMapper.toDomain(u));
  }

  /** Saves a user. */
  @Override
  public User save(User user) {
    return userMapper.toDomain(this.internal.save(userMapper.toEntity(user)));
  }

  /** Checks if a user exists by email or document. */
  @Override
  public boolean existsByEmailOrDocument(String document, String email) {
    return this.internal.existsByDocumentAndEmailAndDeletedAtIsNull(document, email);
  }

  /** Finds all non-deleted users. */
  @Override
  public List<User> findAll() {
    return this.internal.findAllByDeletedAtIsNull().stream()
        .map((u) -> userMapper.toDomain(u))
        .toList();
  }

  /** Finds users by status. */
  @Override
  public List<User> findByStatus(UserStatus status) {
    return this.internal.findByStatusAndDeletedAtIsNull(status).stream()
        .map((u) -> userMapper.toDomain(u))
        .toList();
  }

  /** Counts users by role. */
  @Override
  public long countByRole(UserRole role) {
    return this.internal.countByRole(role);
  }
}
