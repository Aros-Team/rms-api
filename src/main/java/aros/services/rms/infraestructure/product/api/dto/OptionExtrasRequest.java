/* (C) 2026 */

package aros.services.rms.infraestructure.product.api.dto;

import aros.services.rms.core.common.money.domain.Money;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.Currency;

/**
 * Request DTO that pairs an option identifier with the per-association surcharge applied when the
 * option is linked to a product. Used in {@link ProductRequest#optionExtras()}.
 *
 * <p>Example payload: {@code {"optionId": 4, "extraPrice": 2500.00}}.
 */
@Schema(
    description = "Pairs an option id with a surcharge applied when linked to a product",
    example = "{\"optionId\": 4, \"extraPrice\": 2500.00}")
public record OptionExtrasRequest(
    @Schema(description = "Product option identifier", example = "4")
        @NotNull(message = "optionId is required")
        Long optionId,
    @Schema(description = "Per-association surcharge", example = "2500.00")
        @NotNull(message = "extraPrice is required")
        @PositiveOrZero(message = "extraPrice must be zero or positive")
        BigDecimal extraPrice) {

  /**
   * Converts this request DTO to a {@link Money} value in COP. Returns {@code Money.zero(COP)} when
   * the amount is null.
   *
   * @return the surcharge as a {@link Money} in COP
   */
  public Money toMoney() {
    Currency cop = Currency.getInstance("COP");
    return extraPrice == null ? Money.zero(cop) : new Money(extraPrice, cop);
  }
}
