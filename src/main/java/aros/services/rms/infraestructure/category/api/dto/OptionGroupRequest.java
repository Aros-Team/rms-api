/* (C) 2026 */

package aros.services.rms.infraestructure.category.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/** Request DTO for creating or updating an option group. */
@Schema(description = "Request DTO for creating or updating an option group")
public record OptionGroupRequest(
    @Schema(description = "Option group name", example = "Proteína Hamburguesa")
        @NotBlank(message = "Option group name is required")
        String name,
    @Schema(description = "Option group description", example = "Proteínas para hamburguesas")
        String description,
    @Schema(description = "IDs of products this option group applies to", example = "[1, 2, 3]")
        @NotEmpty(message = "At least one product ID is required")
        List<Long> productIds,
    @Schema(
            description = "Whether selecting this option group is mandatory for the product",
            example = "false")
        boolean required,
    @Schema(
            description =
                "Selection mode (optional; defaults to SINGLE_CHOICE when null or blank).",
            example = "SINGLE_CHOICE",
            allowableValues = {"SINGLE_CHOICE", "MULTI_CHOICE", "ADD_ON", "REMOVAL"},
            defaultValue = "SINGLE_CHOICE")
        String selectionType) {}
