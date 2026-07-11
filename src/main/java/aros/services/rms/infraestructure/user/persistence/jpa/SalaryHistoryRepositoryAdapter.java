/* (C) 2026 */

package aros.services.rms.infraestructure.user.persistence.jpa;

import aros.services.rms.core.user.domain.SalaryHistoryEntry;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.core.user.port.output.SalaryHistoryRepositoryPort;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/** Adapter implementing SalaryHistoryRepositoryPort using JPA. */
@Repository
@RequiredArgsConstructor
@Transactional
public class SalaryHistoryRepositoryAdapter implements SalaryHistoryRepositoryPort {

  private final SalaryHistoryJpaRepository internal;
  private final SalaryHistoryPersistenceMapper mapper;

  @Override
  public SalaryHistoryEntry save(SalaryHistoryEntry entry) {
    SalaryHistoryEntity entity = mapper.toEntity(entry);
    if (entity.getChangedAt() == null) {
      entity.setChangedAt(java.time.Instant.now());
    }
    if (entity.getCreatedAt() == null) {
      entity.setCreatedAt(java.time.Instant.now());
    }
    SalaryHistoryEntity saved = internal.save(entity);
    return mapper.toDomain(saved);
  }

  @Override
  public List<SalaryHistoryEntry> findByUserId(UserId userId) {
    return internal.findByUserIdOrderByChangedAtDesc(userId.value()).stream()
        .map(mapper::toDomain)
        .toList();
  }
}
