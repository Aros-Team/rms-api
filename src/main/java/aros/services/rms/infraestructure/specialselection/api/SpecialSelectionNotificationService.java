package aros.services.rms.infraestructure.specialselection.api;

import aros.services.rms.core.common.notification.port.output.NotificationPort;
import aros.services.rms.infraestructure.specialselection.api.dto.SpecialSelectionResponse;
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
   * @param config the updated special selection payload
   */
  public void notifySpecialSelectionUpdated(SpecialSelectionResponse config) {
    try {
      notificationPort.notify(TOPIC_SPECIAL_SELECTIONS_UPDATED, config);
      log.info("Special selection notification sent: productId={}", config.productId());
    } catch (Exception e) {
      log.error(
          "Failed to send special selection notification: productId={}, error={}",
          config.productId(),
          e.getMessage(),
          e);
    }
  }
}
