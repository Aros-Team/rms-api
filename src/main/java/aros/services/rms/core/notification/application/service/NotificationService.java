/* (C) 2026 */

package aros.services.rms.core.notification.application.service;

import aros.services.rms.core.common.notification.port.output.NotificationPort;
import aros.services.rms.core.notification.domain.NotificationCommand;
import aros.services.rms.core.notification.domain.NotificationPage;
import aros.services.rms.core.notification.domain.NotificationSeverity;
import aros.services.rms.core.notification.domain.NotificationView;
import aros.services.rms.core.notification.domain.UserNotification;
import aros.services.rms.core.notification.domain.port.input.NotificationUseCase;
import aros.services.rms.core.notification.domain.port.output.NotificationRepositoryPort;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Orchestrates the persistent global feed and its real-time broadcast. */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService implements NotificationUseCase {

  static final String TOPIC_NOTIFICATIONS = "/topic/notifications";
  private static final int MAX_LIMIT = 100;

  private final NotificationRepositoryPort repositoryPort;
  private final NotificationPort notificationPort;
  private final Clock clock;

  /** {@inheritDoc} */
  @Override
  @Transactional
  public Optional<NotificationView> create(NotificationCommand command) {
    Instant createdAt = command.createdAt() == null ? Instant.now(clock) : command.createdAt();

    UserNotification notification =
        UserNotification.builder()
            .code(command.code())
            .severity(command.severity() == null ? NotificationSeverity.INFO : command.severity())
            .title(command.title())
            .message(command.message())
            .route(command.route())
            .dedupeKey(normalizeDedupeKey(command.dedupeKey()))
            .createdAt(createdAt)
            .build();

    Optional<UserNotification> saved = repositoryPort.insertIfAbsent(notification);
    if (saved.isEmpty()) {
      return Optional.empty();
    }

    NotificationView view = NotificationView.from(saved.get());
    notificationPort.notify(TOPIC_NOTIFICATIONS, view);
    log.info("Notification created: code={}, id={}", view.code(), view.id());
    return Optional.of(view);
  }

  /** {@inheritDoc} */
  @Override
  @Transactional(readOnly = true)
  public NotificationPage list(boolean unreadOnly, int limit, int offset) {
    int safeLimit = Math.max(1, Math.min(limit, MAX_LIMIT));
    int safeOffset = Math.max(0, offset);

    List<NotificationView> items =
        repositoryPort.find(unreadOnly, safeLimit, safeOffset).stream()
            .map(NotificationView::from)
            .toList();

    NotificationPage.PageInfo page =
        new NotificationPage.PageInfo(safeLimit, safeOffset, repositoryPort.count(unreadOnly));

    return new NotificationPage(items, page, repositoryPort.count(true));
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public boolean markRead(Long id) {
    return repositoryPort.markRead(id, Instant.now(clock)) > 0;
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public int markAllRead() {
    return repositoryPort.markAllRead(Instant.now(clock));
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public void clear() {
    repositoryPort.clear();
  }

  private static String normalizeDedupeKey(String dedupeKey) {
    if (dedupeKey == null || dedupeKey.isBlank()) {
      return null;
    }
    return dedupeKey.trim().toUpperCase(Locale.ROOT);
  }
}
