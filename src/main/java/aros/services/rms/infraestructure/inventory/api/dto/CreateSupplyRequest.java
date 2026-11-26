/* (C) 2026 */

package aros.services.rms.infraestructure.inventory.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Request DTO for creating a new supply (insumo base). */
@Schema(
    description = "Request payload for creating a new supply",
    example = "{\"name\": \"Carne de Res\", \"categoryId\": 1}")
public record CreateSupplyRequest(
    @Schema(description = "Supply name", example = "Carne de Res")
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 255, message = "El nombre no debe superar los 255 caracteres")
        String name,
    @Schema(description = "Category ID", example = "1")
        @NotNull(message = "categoryId es obligatorio")
        Long categoryId) {}
