/* (C) 2026 */

package aros.services.rms.core.notification.infrastructure.persistence.adapter;

import aros.services.rms.core.notification.domain.UserNotification;
import aros.services.rms.core.notification.domain.port.output.NotificationRepositoryPort;
import aros.services.rms.core.notification.infrastructure.persistence.entity.UserNotificationEntity;
import aros.services.rms.core.notification.infrastructure.persistence.mapper.UserNotificationMapper;
import aros.services.rms.core.notification.infrastructure.persistence.repository.JpaUserNotificationRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** JPA-backed adapter for the notification feed. */
@Component
@RequiredArgsConstructor
public class NotificationRepositoryAdapter implements NotificationRepositoryPort {

  private final JpaUserNotificationRepository jpaRepo;
  private final UserNotificationMapper mapper;

  /** {@inheritDoc} */
  @Override
  @Transactional
  public Optional<UserNotification> insertIfAbsent(UserNotification notification) {
    String dedupeKey = notification.getDedupeKey();
    if (dedupeKey != null && jpaRepo.existsByDedupeKey(dedupeKey)) {
      return Optional.empty();
    }

    UserNotificationEntity entity = mapper.toEntity(notification);
    entity.setId(null);
    return Optional.of(mapper.toDomain(jpaRepo.save(entity)));
  }

  /** {@inheritDoc} */
  @Override
  public List<UserNotification> find(boolean unreadOnly, int limit, int offset) {
    int fetch = Math.min(offset + limit, 10_000);
    return jpaRepo.findPage(unreadOnly, PageRequest.of(0, fetch)).stream()
        .skip(offset)
        .limit(limit)
        .map(mapper::toDomain)
        .toList();
  }

  /** {@inheritDoc} */
  @Override
  public long count(boolean unreadOnly) {
    return jpaRepo.countNotifications(unreadOnly);
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public int markRead(Long id, Instant readAt) {
    return jpaRepo.markRead(id, readAt);
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public int markAllRead(Instant readAt) {
    return jpaRepo.markAllRead(readAt);
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public void clear() {
    jpaRepo.deleteAllInBatch();
  }
}
