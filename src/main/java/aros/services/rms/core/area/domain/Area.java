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
  private AreaType type;
  @Builder.Default private boolean enabled = true;
  @Builder.Default private Set<Long> orderIds = new HashSet<>();
}
