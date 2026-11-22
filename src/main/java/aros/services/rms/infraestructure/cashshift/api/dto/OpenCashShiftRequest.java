/* (C) 2026 */

package aros.services.rms.infraestructure.cashshift.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Request body to open a shift.
 *
 * @param openingFloat the cash float in the drawer
 */
public record OpenCashShiftRequest(
    @Schema(type = "string", format = "decimal", example = "100000.00")
        @NotNull
        @DecimalMin(value = "0.00")
        BigDecimal openingFloat) {}
