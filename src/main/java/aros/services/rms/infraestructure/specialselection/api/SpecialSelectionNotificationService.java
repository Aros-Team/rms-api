package aros.services.rms.infraestructure.specialselection.api;

import aros.services.rms.core.common.notification.port.output.NotificationPort;
import aros.services.rms.core.specialselection.domain.ChangeType;
import aros.services.rms.infraestructure.specialselection.api.dto.SpecialSelectionResponse;
import aros.services.rms.infraestructure.specialselection.api.dto.SpecialSelectionUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Publishes notifications whenever a special selection configuration changes. */
@Service
@RequiredArgsConstructor
@Slf4j
public class SpecialSelectionNotificationService {

  static final String TOPIC_SPECIAL_SELECTIONS_UPDATED = "/topic/special-selections/updated";

  private final NotificationPort notificationPort;

  /**
   * Sends a notification to subscribers that a special selection has been updated.
   *
   * @param changeType the type of change that occurred
   * @param config the updated special selection payload (null for DELETE events)
   */
  public void notifySpecialSelectionUpdated(
      ChangeType changeType, SpecialSelectionResponse config) {
    SpecialSelectionUpdateEvent event = SpecialSelectionUpdateEvent.of(changeType.name(), config);
    try {
      notificationPort.notify(TOPIC_SPECIAL_SELECTIONS_UPDATED, event);
      log.info(
          "Special selection notification sent: changeType={}, productId={}",
          changeType,
          event.productId());
    } catch (Exception e) {
      log.error(
          "Failed to send special selection notification: changeType={}, productId={}, error={}",
          changeType,
          event.productId(),
          e.getMessage(),
          e);
    }
  }
}
