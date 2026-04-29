/* (C) 2026 */

package aros.services.rms.core.purchase.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain model for supplier / distributor. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Supplier {

  private Long id;
  private String name;
  private String contact;

  @Builder.Default private boolean active = true;
}
