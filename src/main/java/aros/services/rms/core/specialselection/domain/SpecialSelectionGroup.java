package aros.services.rms.core.specialselection.domain;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a group of selectable products within a special selection, grouped by product
 * category.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialSelectionGroup {
  private Long id;
  private int displayOrder;
  private boolean required;
  private int minSelections;
  private int maxSelections;
  private Long categoryId;
  private List<Long> productIds;
}
