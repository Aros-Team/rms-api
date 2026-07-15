package aros.services.rms.core.order.domain;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the customer's answer to a clarification question raised by a special selection
 * product.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClarificationAnswer {
  private Long questionId;
  private String answer;
  private List<String> selectedOptions;
}
