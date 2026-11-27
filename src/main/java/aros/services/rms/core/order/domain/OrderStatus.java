/* (C) 2026 */

package aros.services.rms.core.order.domain;

/**
 * Estados posibles de una orden en el flujo de restaurante. Flujo: QUEUE -> PREPARING -> READY ->
 * DELIVERED
 */
public enum OrderStatus {
  /** Orden cancelada, no se procesa. */
  CANCELLED,
  /** Orden en cola esperando ser preparada. */
  QUEUE,
  /** Orden en preparación por el cocinero. */
  PREPARING,
  /** Todos los productos están preparados; queda pendiente la entrega física. */
  READY,
  /** Orden entregada al cliente. */
  DELIVERED
}
