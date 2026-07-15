package aros.services.rms.infraestructure.specialselection.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import aros.services.rms.core.common.notification.port.output.NotificationPort;
import aros.services.rms.core.specialselection.domain.ChangeType;
import aros.services.rms.infraestructure.specialselection.api.dto.SpecialSelectionResponse;
import aros.services.rms.infraestructure.specialselection.api.dto.SpecialSelectionUpdateEvent;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SpecialSelectionNotificationServiceTest {

  @Mock private NotificationPort notificationPort;

  private SpecialSelectionNotificationService service;

  @BeforeEach
  void setUp() {
    service = new SpecialSelectionNotificationService(notificationPort);
  }

  @Test
  void should_send_envelope_with_change_type_and_selection() throws Exception {
    SpecialSelectionResponse response = buildResponse(7L, true);
    SpecialSelectionUpdateEvent expected =
        SpecialSelectionUpdateEvent.of(ChangeType.UPDATE.name(), response);

    service.notifySpecialSelectionUpdated(ChangeType.UPDATE, response);

    ArgumentCaptor<SpecialSelectionUpdateEvent> captor =
        ArgumentCaptor.forClass(SpecialSelectionUpdateEvent.class);
    verify(notificationPort)
        .notify(
            eq(SpecialSelectionNotificationService.TOPIC_SPECIAL_SELECTIONS_UPDATED),
            captor.capture());
    SpecialSelectionUpdateEvent sent = captor.getValue();
    assert sent.changeType().equals("UPDATE");
    assert sent.productId().equals(7L);
    assert sent.active();
    assert sent.selection() != null;
  }

  @Test
  void should_send_delete_event_with_null_selection() throws Exception {
    service.notifySpecialSelectionUpdated(ChangeType.DELETE, null);

    ArgumentCaptor<SpecialSelectionUpdateEvent> captor =
        ArgumentCaptor.forClass(SpecialSelectionUpdateEvent.class);
    verify(notificationPort)
        .notify(
            eq(SpecialSelectionNotificationService.TOPIC_SPECIAL_SELECTIONS_UPDATED),
            captor.capture());
    SpecialSelectionUpdateEvent sent = captor.getValue();
    assert sent.changeType().equals("DELETE");
    assert sent.productId() == null;
    assert !sent.active();
    assert sent.selection() == null;
  }

  @Test
  void should_not_propagate_exception_when_notification_fails() {
    SpecialSelectionResponse response = buildResponse(1L, true);
    doThrow(new RuntimeException("connection refused")).when(notificationPort).notify(any(), any());

    // Should not throw
    service.notifySpecialSelectionUpdated(ChangeType.CREATE, response);
  }

  private SpecialSelectionResponse buildResponse(Long productId, boolean active) {
    return new SpecialSelectionResponse(
        productId,
        "Test Selection",
        "Desc",
        10.0,
        active,
        1L,
        "SPECIAL_SELECTION",
        false,
        false,
        Collections.emptyList(),
        Collections.emptyList(),
        Collections.emptyList(),
        Collections.emptyList());
  }
}
