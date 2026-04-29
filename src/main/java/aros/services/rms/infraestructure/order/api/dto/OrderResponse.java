/* (C) 2026 */

package aros.services.rms.infraestructure.order.api.dto;

import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderDetail;
import aros.services.rms.core.product.domain.ProductOption;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/** Response DTO for order data. */
@Schema(description = "Response DTO para datos de una orden")
public record OrderResponse(
    @Schema(description = "ID de la orden", example = "1") Long id,
    @Schema(description = "Fecha y hora de la orden", example = "2026-03-08T14:30:00")
        LocalDateTime date,
    @Schema(description = "Estado de la orden", example = "QUEUE") String status,
    @Schema(description = "ID de la mesa", example = "1") Long tableId,
    @Schema(description = "Lista de productos en la orden") List<OrderDetailResponse> details) {

  /** Detail of a product in the order. */
  @Schema(description = "Detalle de un producto en la orden")
  public record OrderDetailResponse(
      @Schema(description = "ID del detalle", example = "1") Long id,
      @Schema(description = "ID del producto", example = "1") Long productId,
      @Schema(description = "Nombre del producto", example = "Hamburguesa Clásica")
          String productName,
      @Schema(description = "Precio unitario", example = "12.50") Double unitPrice,
      @Schema(description = "Instrucciones especiales", example = "Sin cebolla")
          String instructions,
      @Schema(description = "Opciones seleccionadas del producto")
          List<ProductOptionResponse> selectedOptions) {}

  /** Selected product option. */
  @Schema(description = "Opción de producto seleccionada")
  public record ProductOptionResponse(
      @Schema(description = "ID de la opción", example = "1") Long id,
      @Schema(description = "Nombre de la opción", example = "Grande") String name,
      @Schema(description = "Nombre de la categoría", example = "Tamaños") String categoryName) {}

  /**
   * Creates a response from a domain object.
   *
   * @param order the order domain
   * @return the response DTO
   */
  public static OrderResponse fromDomain(Order order) {
    if (order == null) {
      return null;
    }

    return new OrderResponse(
        order.getId(),
        order.getDate(),
        order.getStatus() != null ? order.getStatus().name() : null,
        order.getTable() != null ? order.getTable().getId() : null,
        order.getDetails() != null
            ? order.getDetails().stream()
                .map(OrderResponse::fromDomainDetail)
                .collect(Collectors.toList())
            : null);
  }

  private static OrderDetailResponse fromDomainDetail(OrderDetail detail) {
    if (detail == null) {
      return null;
    }

    return new OrderDetailResponse(
        detail.getId(),
        detail.getProduct() != null ? detail.getProduct().getId() : null,
        detail.getProduct() != null ? detail.getProduct().getName() : null,
        detail.getUnitPrice(),
        detail.getInstructions(),
        detail.getSelectedOptions() != null
            ? detail.getSelectedOptions().stream()
                .map(OrderResponse::fromDomainOption)
                .collect(Collectors.toList())
            : null);
  }

  private static ProductOptionResponse fromDomainOption(ProductOption option) {
    if (option == null) {
      return null;
    }

    return new ProductOptionResponse(
        option.getId(),
        option.getName(),
        option.getCategory() != null ? option.getCategory().getName() : null);
  }
}
