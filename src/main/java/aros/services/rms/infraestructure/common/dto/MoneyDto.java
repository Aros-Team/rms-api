/* (C) 2026 */

package aros.services.rms.infraestructure.common.dto;

import aros.services.rms.core.common.money.domain.CurrencyContext;
import aros.services.rms.core.common.money.domain.Money;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * THE standard monetary wire type for the RMS API.
 *
 * <p>Every monetary value exposed over HTTP uses this shape: {@code
 * {"amount":"123.45","currency":"COP"}}. The {@code amount} is always a decimal string (never a
 * JSON number) so no floating-point precision is lost, and {@code currency} is an ISO 4217 code.
 * This is the single monetary contract for the API; response DTOs never expose the domain {@link
 * Money} directly.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Monetary amount with currency")
public class MoneyDto {

  /** Bootstrap currency used before the persisted setting is installed. */
  public static final String DEFAULT_CURRENCY = CurrencyContext.BOOTSTRAP_CURRENCY_CODE;

  @Schema(description = "Monetary amount as decimal string", example = "125000000.00")
  private String amount;

  @Schema(description = "ISO 4217 currency code", example = "COP")
  private String currency;

  /**
   * Creates a wire DTO from a domain {@link Money}.
   *
   * @param money the domain money (may be null)
   * @return the DTO, or {@code null} when {@code money} is null
   */
  public static MoneyDto of(Money money) {
    if (money == null) {
      return null;
    }
    return new MoneyDto(money.amount().toPlainString(), money.currency().getCurrencyCode());
  }

  /**
   * Creates a wire DTO from a raw amount and an ISO 4217 currency code.
   *
   * @param amount the raw amount (may be null)
   * @param currency the ISO 4217 currency code
   * @return the DTO, or {@code null} when {@code amount} is null
   */
  public static MoneyDto of(BigDecimal amount, String currency) {
    if (amount == null) {
      return null;
    }
    return new MoneyDto(amount.toPlainString(), currency);
  }

  /**
   * Creates a wire DTO from a raw amount using the configured system currency.
   *
   * @param amount the raw amount (may be null)
   * @return the DTO, or {@code null} when {@code amount} is null
   */
  public static MoneyDto of(BigDecimal amount) {
    return of(amount, CurrencyContext.get().getCurrencyCode());
  }
}
