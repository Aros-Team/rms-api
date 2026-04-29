/* (C) 2026 */

package aros.services.rms.core.inventory.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain model for storage location - physical storage areas (e.g., Bodega, Cocina). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageLocation {

  private Long id;
  private String name;
}
