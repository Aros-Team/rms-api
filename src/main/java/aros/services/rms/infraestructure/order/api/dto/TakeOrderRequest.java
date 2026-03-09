/* (C) 2026 */
package aros.services.rms.infraestructure.order.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(
    description = "Request DTO para crear una nueva orden",
    example =
        """
        {
          "tableId": 1,
          "details": [
            {
              "productId": 1,
              "instructions": "Sin cebolla",
              "selectedOptionIds": [1, 2]
            }
          ]
        }
        """)
public record TakeOrderRequest(
    @Schema(description = "ID de la mesa", example = "1") @NotNull(message = "Table ID is required") Long tableId,
    @Schema(description = "Lista de productos ordenados")
        @NotEmpty(message = "Order details cannot be empty") @Valid List<OrderDetailRequest> details) {

  @Schema(description = "Detalle de un producto en la orden")
  public record OrderDetailRequest(
      @Schema(description = "ID del producto", example = "1")
          @NotNull(message = "Product ID is required") Long productId,
      @Schema(description = "Instrucciones especiales para el producto", example = "Sin cebolla")
          String instructions,
      @Schema(description = "IDs de opciones seleccionadas del producto", example = "[1, 2]")
          List<Long> selectedOptionIds) {}
}
