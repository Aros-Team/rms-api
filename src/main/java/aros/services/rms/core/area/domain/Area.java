/* (C) 2026 */
package aros.services.rms.core.area.domain;

import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Area {
  private Long id;
  private String name;
  private AreaType type;
  @Builder.Default private Set<Long> orderIds = new HashSet<>();
}
