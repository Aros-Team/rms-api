/* (C) 2026 */

package aros.services.rms.infraestructure.floorlayout.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entity for one independent region on the floor layout. */
@Entity
@Table(name = "floor_layout_region")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FloorRegionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "floor_layout_id", nullable = false)
  private Long floorLayoutId;

  @Column(name = "region_key", nullable = false, length = 64)
  private String regionKey;

  @Column(name = "area_id", nullable = false)
  private Long areaId;

  @Lob
  @Column(name = "segments_json", nullable = false, columnDefinition = "LONGTEXT")
  private String segmentsJson;
}
