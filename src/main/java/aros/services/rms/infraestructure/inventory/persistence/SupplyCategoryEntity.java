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

/** Categoría de insumos (ej: Proteínas, Vegetales, Lácteos). */
@Entity
@Table(name = "supply_categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplyCategoryEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String name;
}
