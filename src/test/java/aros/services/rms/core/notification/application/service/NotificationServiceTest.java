/* (C) 2026 */

package aros.services.rms.core.notification.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.common.notification.port.output.NotificationPort;
import aros.services.rms.core.notification.domain.NotificationCommand;
import aros.services.rms.core.notification.domain.NotificationPage;
import aros.services.rms.core.notification.domain.NotificationSeverity;
import aros.services.rms.core.notification.domain.NotificationView;
import aros.services.rms.core.notification.domain.UserNotification;
import aros.services.rms.core.notification.domain.port.output.NotificationRepositoryPort;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link NotificationService}.
 *
 * <p><b>Feature:</b> Persistent global notification feed.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>a persisted notification is broadcast on the STOMP topic
 *   <li>a duplicate dedupe key produces no row and no broadcast
 *   <li>the page size is clamped to 100 before hitting the repository
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  private static final Instant NOW = Instant.parse("2026-11-22T20:00:00Z");

  @Mock private NotificationRepositoryPort repositoryPort;
  @Mock private NotificationPort notificationPort;

  private NotificationService service() {
    return new NotificationService(
        repositoryPort, notificationPort, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void shouldBroadcastPersistedNotification() {
    UserNotification saved =
        UserNotification.builder()
            .id(7L)
            .code("ORDER_READY")
            .severity(NotificationSeverity.INFO)
            .title("Pedido listo")
            .message("Pedido #7 · Mesa 3")
            .route("/admin/orders")
            .createdAt(NOW)
            .build();
    when(repositoryPort.insertIfAbsent(any(UserNotification.class))).thenReturn(Optional.of(saved));

    Optional<NotificationView> view =
        service()
            .create(
                new NotificationCommand(
                    "ORDER_READY",
                    NotificationSeverity.INFO,
                    "Pedido listo",
                    "Pedido #7 · Mesa 3",
                    "/admin/orders",
                    "order_ready:7"));

    assertTrue(view.isPresent());
    assertEquals(7L, view.get().id());
    assertTrue(!view.get().read());
    verify(notificationPort)
        .notify(eq(NotificationService.TOPIC_NOTIFICATIONS), any(NotificationView.class));
  }

  @Test
  void shouldNotBroadcastDuplicate() {
    when(repositoryPort.insertIfAbsent(any(UserNotification.class))).thenReturn(Optional.empty());

    Optional<NotificationView> view =
        service()
            .create(
                new NotificationCommand(
                    "ORDER_READY",
                    NotificationSeverity.INFO,
                    "Pedido listo",
                    "Pedido #7 · Mesa 3",
                    "/admin/orders",
                    "ORDER_READY:7"));

    assertTrue(view.isEmpty());
    verify(notificationPort, never()).notify(any(), any());
  }

  @Test
  void should_clamp_the_page_size() {
    when(repositoryPort.find(false, 100, 0)).thenReturn(List.of());
    when(repositoryPort.count(false)).thenReturn(0L);
    when(repositoryPort.count(true)).thenReturn(0L);

    NotificationPage page = service().list(false, 500, -10);

    assertEquals(100, page.page().limit());
    assertEquals(0, page.page().offset());
  }
}
