/* (C) 2026 */
package aros.services.rms.core.inventory.domain;

/**
 * Tipos de movimiento de inventario.
 *
 * <ul>
 *   <li>{@code TRANSFER} – Traslado entre ubicaciones (Bodega → Cocina).
 *   <li>{@code DEDUCTION} – Consumo por venta (se descuenta de Cocina).
 *   <li>{@code ENTRY} – Ingreso inicial o por compra.
 * </ul>
 */
public enum MovementType {
  TRANSFER,
  DEDUCTION,
  ENTRY
}
