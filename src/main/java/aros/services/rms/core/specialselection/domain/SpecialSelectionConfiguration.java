package aros.services.rms.core.specialselection.domain;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Aggregates the full configuration of a special selection product, including groups, additions,
 * questions and availability schedule.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialSelectionConfiguration {
  private Long productId;
  private String name;
  private String description;
  private Double basePrice;
  private boolean active;
  private Long preparationAreaId;
  private SelectionType selectionType;
  private boolean baseRecipeEnabled;
  private boolean schedulingRequired;
  private List<SpecialSelectionGroup> groups;
  private List<SpecialSelectionAddition> additions;
  private List<SpecialSelectionQuestion> questions;
  private List<SpecialSelectionScheduleEntry> schedule;
}
