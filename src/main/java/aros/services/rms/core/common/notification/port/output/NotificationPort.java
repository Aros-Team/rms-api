package aros.services.rms.core.common.notification.port.output;

/**
 * Output port para el envío de notificaciones en tiempo real.
 *
 * <p>Define el contrato que el core utiliza para publicar eventos a clientes conectados,
 * sin conocimiento del transporte subyacente (WebSocket, SSE, etc.).
 *
 * <p>Las implementaciones residen exclusivamente en la capa de infraestructura.
 * El core nunca importa clases de {@code org.springframework.*} a través de este port.
 */
public interface NotificationPort {

  /**
   * Publica un mensaje en el destino indicado.
   *
   * <p>La implementación es responsable de serializar {@code payload} al formato
   * adecuado (JSON) y de manejar cualquier error de transporte sin propagar
   * excepciones al llamador.
   *
   * @param destination ruta del destino STOMP, p.ej. {@code /topic/orders/ready}
   * @param payload     objeto a serializar y enviar; no debe ser {@code null}
   */
  void notify(String destination, Object payload);
}
