/* (C) 2026 */
package aros.services.rms.infraestructure.product.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(
    description = "Request DTO para crear o actualizar una opción de producto",
    example =
        """
        {
          "name": "Grande (1.5L)",
          "optionCategoryId": 1
        }
        """)
public record ProductOptionRequest(
    @Schema(description = "Nombre de la opción", example = "Grande (1.5L)")
        @NotBlank(message = "Option name is required")
        String name,
    @Schema(description = "ID de la categoría de opción a la que pertenece", example = "1")
        @NotNull(message = "Option category ID is required")
        Long optionCategoryId) {}