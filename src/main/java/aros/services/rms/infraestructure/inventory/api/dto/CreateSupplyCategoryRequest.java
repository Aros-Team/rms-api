/* (C) 2026 */
package aros.services.rms.infraestructure.inventory.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request DTO for creating a new supply category. */
@Schema(description = "Request payload for creating a supply category")
public record CreateSupplyCategoryRequest(
    @Schema(description = "Category name", example = "Proteínas")
        @NotBlank(message = "name is required")
        @Size(max = 255, message = "name must not exceed 255 characters")
        String name) {}
