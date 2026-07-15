package aros.services.rms.core.specialselection.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a clarification question that the customer must answer when ordering a special
 * selection.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialSelectionQuestion {
  private Long id;
  private Long productId;
  private String question;
  private boolean required;
  private int displayOrder;
  @Builder.Default private QuestionType questionType = QuestionType.TEXT;
}
