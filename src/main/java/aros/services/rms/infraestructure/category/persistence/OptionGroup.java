/* (C) 2026 */

package aros.services.rms.infraestructure.category.persistence;

import aros.services.rms.core.category.domain.OptionSelectionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing an option group.
 *
 * <p>Persists the {@code selection_type} (V37) and optional {@code replace_supply_category_id}
 * foreign key (V37) columns. The selection type is stored as the enum constant name via {@link
 * EnumType#STRING}.
 */
@Entity
@Table(name = "option_group")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptionGroup {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  private String description;

  @Column(name = "selection_type", nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private OptionSelectionType selectionType;

  @Column(name = "replace_supply_category_id")
  private Long replaceSupplyCategoryId;
}
