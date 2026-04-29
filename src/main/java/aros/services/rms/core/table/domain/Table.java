/* (C) 2026 */

package aros.services.rms.core.table.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain model representing a restaurant table with its status tracking availability. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Table {
  private Long id;
  private Integer tableNumber;
  private Integer capacity;
  private TableStatus status;
}
