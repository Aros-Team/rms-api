package aros.services.rms.core.specialselection.domain;

import java.time.LocalTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Serializable snapshot of a special selection configuration, including its groups, additions,
 * questions and schedule. Used to persist historical versions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialSelectionSnapshot {
  private String name;
  private String description;
  private Double basePrice;
  private String selectionType;
  private boolean baseRecipeEnabled;
  private boolean schedulingRequired;
  private boolean active;
  private List<GroupSnapshot> groups;
  private List<AdditionSnapshot> additions;
  private List<QuestionSnapshot> questions;
  private List<ScheduleEntrySnapshot> schedule;

  /** Snapshot of a product-category group with its contained product IDs. */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class GroupSnapshot {
    private Long id;
    private Long categoryId;
    private boolean required;
    private int minSelections;
    private int maxSelections;
    private int displayOrder;
    private List<Long> productIds;
  }

  /** Snapshot of an optional addition that can be applied to a special selection. */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class AdditionSnapshot {
    private Long id;
    private String name;
    private Double extraPrice;
    private Long optionId;
    private int displayOrder;
  }

  /** Snapshot of a clarification question raised by the special selection. */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class QuestionSnapshot {
    private Long id;
    private String question;
    private boolean required;
    private int displayOrder;
    private String questionType;
  }

  /** Snapshot of a weekly availability window for the special selection. */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ScheduleEntrySnapshot {
    private Long id;
    private String dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
  }
}
