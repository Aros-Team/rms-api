package aros.services.rms.infraestructure.specialselection.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** Request payload carrying the desired profit margin for a suggested price calculation. */
@Schema(description = "Request to suggest a price for a special selection")
public record SuggestedPriceRequest(
    @Schema(description = "Desired profit margin percentage", example = "30") @NotNull
        BigDecimal marginPercent) {}
