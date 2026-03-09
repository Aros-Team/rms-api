/* (C) 2026 */
package aros.services.rms.infraestructure.category.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(
    description = "Request DTO for creating or updating a product category",
    example =
        """
        {
          "name": "Hamburguesas",
          "description": "Todas las hamburguesas del menú"
        }
        """)
public record CategoryRequest(
    @Schema(description = "Category name", example = "Hamburguesas")
        @NotBlank(message = "Category name is required")
        String name,
    @Schema(description = "Category description", example = "Todas las hamburguesas del menú")
        String description) {}