/* (C) 2026 */

package aros.services.rms.infraestructure.payment.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

/**
 * Request body for a charge.
 *
 * <p>There is intentionally no subtotal or total field: the server derives both from the order.
 *
 * @param orderId the delivered order to charge
 * @param tipAmount the tip, optional
 * @param discount the optional single manual discount
 * @param tenders one or more tenders (split payment)
 */
public record RegisterPaymentRequest(
    @NotNull Long orderId,
    @Schema(type = "string", format = "decimal", example = "5000.00") @DecimalMin(value = "0.00")
        BigDecimal tipAmount,
    @Valid DiscountRequest discount,
    @NotEmpty @Valid List<TenderRequest> tenders) {

  /**
   * One manual discount.
   *
   * @param type PERCENT or AMOUNT
   * @param value the raw percentage or amount
   * @param reason the mandatory justification
   */
  public record DiscountRequest(
      @NotBlank String type,
      @Schema(
              type = "string",
              format = "decimal",
              description = "Percentage (10.00) or amount (5000.00) as a decimal string",
              example = "10.00")
          @NotNull
          @DecimalMin(value = "0.01")
          BigDecimal value,
      @NotBlank @Size(max = 255) String reason) {}

  /**
   * One tender of the payment.
   *
   * @param method CASH, CARD or TRANSFER
   * @param amount the amount entered
   * @param reference optional card or transfer reference
   */
  public record TenderRequest(
      @NotBlank String method,
      @Schema(type = "string", format = "decimal", example = "58000.00")
          @NotNull
          @DecimalMin(value = "0.01")
          BigDecimal amount,
      @Size(max = 100) String reference) {}
}
