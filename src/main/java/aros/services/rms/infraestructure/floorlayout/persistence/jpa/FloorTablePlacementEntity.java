/* (C) 2026 */

package aros.services.rms.infraestructure.floorlayout.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entity for one table placed on the floor layout. */
@Entity
@Table(name = "floor_layout_table")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FloorTablePlacementEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "floor_layout_id", nullable = false)
  private Long floorLayoutId;

  @Column(name = "table_id", nullable = false)
  private Long tableId;

  @Column(name = "row_index", nullable = false)
  private int row;

  @Column(name = "column_index", nullable = false)
  private int column;
}
