/* (C) 2026 */

package aros.services.rms.infraestructure.order.persistence;

/** Order status enumeration. */
public enum OrderStatus {
  CANCELLED,
  QUEUE,
  PREPARING,
  READY,
  DELIVERED
}
