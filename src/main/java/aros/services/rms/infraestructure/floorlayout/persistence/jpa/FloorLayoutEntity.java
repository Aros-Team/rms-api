/* (C) 2026 */

package aros.services.rms.infraestructure.floorlayout.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entity for the singleton floor layout row. */
@Entity
@Table(name = "floor_layout")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FloorLayoutEntity {

  @Id private Long id;

  @Column(name = "columns_count", nullable = false)
  private int columns;

  @Column(name = "rows_count", nullable = false)
  private int rows;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
