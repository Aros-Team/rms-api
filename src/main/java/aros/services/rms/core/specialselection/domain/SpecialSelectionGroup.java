package aros.services.rms.core.specialselection.domain;

import aros.services.rms.core.product.domain.ProductOption;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a group of selectable options within a special selection, with constraints on the
 * number of selections allowed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialSelectionGroup {
  private Long id;
  private Long productId;
  private String name;
  private int displayOrder;
  private boolean required;
  private int minSelections;
  private int maxSelections;
  private List<ProductOption> options;
}
