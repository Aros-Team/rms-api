package aros.services.rms.infraestructure.specialselection.persistence;

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

/**
 * JPA entity representing a clarification question that can be asked when ordering a special
 * selection.
 */
@Entity
@Table(name = "special_selection_questions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialSelectionQuestionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "product_id", nullable = false)
  private Long productId;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String question;

  @Column(nullable = false)
  @Builder.Default
  private boolean required = false;

  @Column(name = "display_order")
  @Builder.Default
  private int displayOrder = 0;
}
