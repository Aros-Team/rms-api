/* (C) 2026 */

package aros.services.rms.infraestructure.table.api;

import aros.services.rms.core.common.notification.port.output.NotificationPort;
import aros.services.rms.infraestructure.table.api.dto.TableResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Servicio de notificaciones en tiempo real para mesas. Publica eventos de cambio de estado de
 * mesas a los clientes WebSocket suscritos a través del {@link NotificationPort}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TableNotificationService {

  static final String TOPIC_TABLES_STATUS = "/topic/tables/status";

  private final NotificationPort notificationPort;

  /**
   * Notifica a los clientes que una mesa cambió de estado.
   *
   * @param table el {@link TableResponse} con el nuevo estado de la mesa
   */
  public void notifyTableStatusChanged(TableResponse table) {
    log.info(
        "WebSocket: notificando cambio de estado de mesa id={} status={}",
        table.id(),
        table.status());
    notificationPort.notify(TOPIC_TABLES_STATUS, table);
  }
}
