/* (C) 2026 */

package aros.services.rms.core.inventory.application.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * Evento publicado vía WebSocket cuando el stock de inventario cambia tras el descuento de una
 * orden.
 *
 * <p>Contiene únicamente los insumos afectados (Opción B: solo los cambios), lo que minimiza el
 * payload enviado al frontend.
 */
@Data
@Builder
public class InventoryStockUpdatedEvent {

  /** Tipo de evento, siempre {@code "INVENTORY_UPDATED"}. */
  @Builder.Default private final String type = "INVENTORY_UPDATED";

  /** Lista de variantes cuyo stock cambió. */
  private final List<UpdatedStockItem> updatedItems;

  /** Representa el nuevo estado de stock de una variante en una ubicación. */
  @Data
  @Builder
  public static class UpdatedStockItem {

    /** ID de la variante de insumo. */
    private final Long supplyVariantId;

    /** ID de la ubicación de almacenamiento (Cocina o Bodega). */
    private final Long storageLocationId;

    /** Nombre de la ubicación (ej: "Cocina", "Bodega"). */
    private final String locationName;

    /** Cantidad actual tras el descuento. */
    private final BigDecimal currentQuantity;
  }
}
