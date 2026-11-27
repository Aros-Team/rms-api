/* (C) 2026 */

package aros.services.rms.core.area.domain;

import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain model representing a preparation area in the restaurant (e.g., kitchen, bartender). Areas
 * can be enabled or disabled to control whether they receive new products.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Area {
  private Long id;
  private String name;

  /** Activities that can be performed in this physical area. */
  @Builder.Default private Set<AreaActivity> activities = new HashSet<>();

  private boolean forUser;
  private boolean forProduct;

  /** An exclusive area cannot be combined with any other area for a worker. */
  @Builder.Default private boolean exclusive = false;

  @Builder.Default private boolean enabled = true;
  @Builder.Default private Set<Long> orderIds = new HashSet<>();
}
