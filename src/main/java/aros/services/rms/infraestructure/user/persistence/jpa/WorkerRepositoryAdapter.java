/* (C) 2026 */

package aros.services.rms.infraestructure.user.persistence.jpa;

import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserRole;
import aros.services.rms.core.user.port.output.WorkerRepositoryPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/** Adapter for WorkerRepositoryPort. */
@Repository
@RequiredArgsConstructor
public class WorkerRepositoryAdapter implements WorkerRepositoryPort {
  @Autowired private JpaUserRepository internal;

  @Autowired private UserPersistenceMapper userMapper;

  @Override
  public List<User> findAllWorkers() {
    return internal.findByRoleAndDeletedAtIsNull(UserRole.WORKER).stream()
        .map(userMapper::toDomain)
        .toList();
  }
}
