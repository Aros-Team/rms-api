/* (C) 2026 */

package aros.services.rms.infraestructure.cashshift.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Request body to close a shift.
 *
 * @param countedCash the cash counted by the cashier
 * @param notes optional close notes
 */
public record CloseCashShiftRequest(
    @NotNull @DecimalMin(value = "0.00") BigDecimal countedCash, @Size(max = 500) String notes) {}
