/* (C) 2026 */

package aros.services.rms.core.inventory.domain;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain model for supply variant - physical presentation of a supply. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplyVariant {

  private Long id;
  private Long supplyId;
  private Long unitId;
  private BigDecimal quantity;
}
