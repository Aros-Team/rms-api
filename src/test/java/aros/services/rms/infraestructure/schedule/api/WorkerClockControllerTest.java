/* (C) 2026 */

package aros.services.rms.infraestructure.schedule.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.notification.domain.NotificationCommand;
import aros.services.rms.core.notification.domain.NotificationSeverity;
import aros.services.rms.core.notification.domain.port.input.NotificationUseCase;
import aros.services.rms.core.schedule.domain.LogType;
import aros.services.rms.core.schedule.domain.TimeLog;
import aros.services.rms.core.schedule.domain.TimeLogId;
import aros.services.rms.core.schedule.port.input.ClockInUseCase;
import aros.services.rms.core.schedule.port.input.ClockOutUseCase;
import aros.services.rms.core.systemconfig.domain.port.output.BusinessTimeZoneProvider;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Tests for the clock-in notification produced by {@link WorkerClockController}.
 *
 * <p><b>Feature:</b> the admin feed learns when a worker marks entry.
 *
 * <p><b>Expected behavior:</b> a successful clock-in publishes one {@code WORKER_CLOCK_IN}
 * notification with the worker name and local time, and keeps the dedupe key tied to the entry.
 */
@ExtendWith(MockitoExtension.class)
class WorkerClockControllerTest {

  @Mock private ClockInUseCase clockInUseCase;
  @Mock private ClockOutUseCase clockOutUseCase;
  @Mock private UserRepositoryPort userRepositoryPort;
  @Mock private NotificationUseCase notificationUseCase;
  @Mock private BusinessTimeZoneProvider timeZoneProvider;

  @InjectMocks private WorkerClockController controller;

  @Test
  void shouldPublishClockInNotificationForAuthenticatedWorker() {
    when(timeZoneProvider.getZoneId()).thenReturn(ZoneOffset.UTC);
    Jwt jwt = mock(Jwt.class);
    when(jwt.getSubject()).thenReturn("ana@rms.local");

    User worker = mock(User.class);
    when(worker.getId()).thenReturn(UserId.of(5L));
    when(worker.getName()).thenReturn("Ana Pérez");
    when(userRepositoryPort.findByEmail("ana@rms.local")).thenReturn(Optional.of(worker));

    TimeLog entry =
        new TimeLog(
            TimeLogId.of(99L),
            UserId.of(5L),
            Instant.parse("2026-11-23T12:30:00Z"),
            LogType.IN,
            true,
            11L);
    when(clockInUseCase.clockIn(UserId.of(5L))).thenReturn(entry);

    controller.clockIn(jwt);

    ArgumentCaptor<NotificationCommand> captor = ArgumentCaptor.forClass(NotificationCommand.class);
    verify(notificationUseCase).create(captor.capture());
    NotificationCommand command = captor.getValue();
    assertEquals("WORKER_CLOCK_IN", command.code());
    assertEquals(NotificationSeverity.INFO, command.severity());
    assertTrue(command.message().contains("Ana Pérez"));
    assertTrue(command.message().contains("12:30"));
    assertEquals("CLOCK_IN:99", command.dedupeKey());
  }
}
