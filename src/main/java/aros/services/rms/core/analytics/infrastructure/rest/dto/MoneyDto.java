/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Money DTO matching the {amount, currency} shape defined in the A6 contract. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Monetary amount with currency")
public class MoneyDto {

  @Schema(description = "Monetary amount as decimal string", example = "125000000.00")
  private String amount;

  @Schema(description = "ISO 4217 currency code", example = "COP")
  private String currency;
}
