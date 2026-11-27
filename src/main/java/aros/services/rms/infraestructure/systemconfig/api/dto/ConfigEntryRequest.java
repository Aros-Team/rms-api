/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request DTO to update a configuration entry. */
@Schema(
    description = "Request to update a configuration entry",
    example = "{\"key\": \"default_currency\", \"value\": \"USD\"}")
public record ConfigEntryRequest(
    @Schema(description = "Configuration key", example = "default_currency")
        @NotBlank(message = "La clave no puede estar vacía")
        @Size(max = 120, message = "La clave no debe superar los 120 caracteres")
        String key,
    @Schema(description = "Configuration value", example = "USD")
        @NotBlank(message = "El valor no puede estar vacío")
        @Size(max = 500, message = "El valor no debe superar los 500 caracteres")
        String value,
    @Schema(
            description =
                "Group ID. Existing keys keep their current group when omitted; new keys use the "
                    + "first group.",
            example = "1")
        Long groupId,
    @Schema(
            description =
                "Sort order within the group. Existing keys keep their current order when omitted; "
                    + "new keys are appended after the group's last entry.",
            example = "1")
        Integer sortOrder) {}
