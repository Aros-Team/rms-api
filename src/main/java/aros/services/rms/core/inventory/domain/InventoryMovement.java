/* (C) 2026 */

package aros.services.rms.core.inventory.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain model for inventory movement - tracks transfers and deductions. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMovement {

  private Long id;
  private Long supplyVariantId;
  private Long fromStorageLocationId;
  private Long toStorageLocationId;
  private BigDecimal quantity;
  private MovementType movementType;
  private Long referenceOrderId;
  private Long referencePurchaseOrderId;
  private LocalDateTime createdAt;
}
