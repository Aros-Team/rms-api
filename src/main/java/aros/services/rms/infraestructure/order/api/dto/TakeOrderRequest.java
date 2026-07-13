/* (C) 2026 */

package aros.services.rms.infraestructure.order.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/** Request DTO for creating a new order. */
@Schema(
    description = "Request DTO to create a new order",
    example =
        "{\"tableId\": 1, \"details\": [{\"productId\": 1, \"instructions\": \"No onions\", "
            + "\"selectedOptionIds\": [1, 2]}]}")
public record TakeOrderRequest(
    @Schema(description = "Table ID", example = "1") @NotNull(message = "Table ID is required")
        Long tableId,
    @Schema(description = "List of ordered products")
        @NotEmpty(message = "Order details cannot be empty")
        @Valid
        List<OrderDetailRequest> details) {

  /** Detail of a product in the order. */
  @Schema(description = "Detail of a product in the order")
  public record OrderDetailRequest(
      @Schema(description = "Product ID", example = "1")
          @NotNull(message = "Product ID is required")
          Long productId,
      @Schema(description = "Special instructions for the product", example = "No onions")
          String instructions,
      @Schema(description = "Selected product option IDs", example = "[1, 2]")
          List<Long> selectedOptionIds) {}
}
