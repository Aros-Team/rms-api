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
@Schema(description = "Response DTO for order data")
public record OrderResponse(
    @Schema(description = "Order ID", example = "1") Long id,
    @Schema(description = "Order date and time", example = "2026-03-08T14:30:00")
        LocalDateTime date,
    @Schema(description = "Order status", example = "QUEUE") String status,
    @Schema(description = "Table ID", example = "1") Long tableId,
    @Schema(description = "List of products in the order") List<OrderDetailResponse> details) {

  /** Detail of a product in the order. */
  @Schema(description = "Detail of a product in the order")
  public record OrderDetailResponse(
      @Schema(description = "Detail ID", example = "1") Long id,
      @Schema(description = "Product ID", example = "1") Long productId,
      @Schema(description = "Product name", example = "Classic Burger") String productName,
      @Schema(description = "Unit price", example = "12.50") Double unitPrice,
      @Schema(description = "Special instructions", example = "No onions") String instructions,
      @Schema(description = "Selected product options")
          List<ProductOptionResponse> selectedOptions) {}

  /** Selected product option. */
  @Schema(description = "Selected product option")
  public record ProductOptionResponse(
      @Schema(description = "Option ID", example = "1") Long id,
      @Schema(description = "Option name", example = "Large") String name,
      @Schema(description = "Category name", example = "Sizes") String categoryName) {}

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
