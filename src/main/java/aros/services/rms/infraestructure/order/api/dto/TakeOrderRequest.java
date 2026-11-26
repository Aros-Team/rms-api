package aros.services.rms.infraestructure.order.api.dto;

import aros.services.rms.core.order.domain.ClarificationAnswer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
    @Schema(description = "Table ID", example = "1")
        @NotNull(message = "El ID de mesa es obligatorio")
        Long tableId,
    @Schema(description = "Number of guests (covers)", example = "4")
        @Min(value = 1, message = "El número de comensales debe ser al menos 1")
        @Max(value = 20, message = "El número de comensales debe ser como máximo 20")
        Integer partySize,
    @Schema(description = "List of ordered products")
        @NotEmpty(message = "Los detalles del pedido no pueden estar vacíos")
        @Valid
        List<OrderDetailRequest> details) {

  /** Detail of a product in the order. */
  @Schema(description = "Detail of a product in the order")
  public record OrderDetailRequest(
      @Schema(description = "Product ID", example = "1")
          @NotNull(message = "El ID del producto es obligatorio")
          Long productId,
      @Schema(description = "Special instructions for the product", example = "No onions")
          String instructions,
      @Schema(description = "Selected product option IDs", example = "[1, 2]")
          List<Long> selectedOptionIds,
      @Schema(description = "Selected product IDs from combo groups", example = "[1, 2, 3]")
          List<Long> selectedProductIds,
      @Schema(description = "Special selection addition IDs", example = "[1, 2]")
          List<Long> additionIds,
      @Schema(description = "Clarification answers") List<ClarificationAnswer> clarifications) {}
}
