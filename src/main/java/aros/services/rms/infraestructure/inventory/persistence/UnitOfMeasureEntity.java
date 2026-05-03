/* (C) 2026 */

package aros.services.rms.infraestructure.inventory.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Unidad de medida estandarizada (ej: Gramos, Mililitros, Unidades). */
@Entity
@Table(name = "units_of_measure")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitOfMeasureEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 100)
  private String name;

  @Column(nullable = false, unique = true, length = 20)
  private String abbreviation;
}
