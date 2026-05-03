/* (C) 2026 */

package aros.services.rms.core.inventory.domain;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain model for inventory stock - current quantity of a supply variant in a storage location.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryStock {

  private Long id;
  private Long supplyVariantId;
  private Long storageLocationId;
  private BigDecimal currentQuantity;
}
