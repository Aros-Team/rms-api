package aros.services.rms.core.specialselection.application.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import aros.services.rms.core.order.domain.ClarificationAnswer;
import aros.services.rms.core.specialselection.application.exception.InvalidSpecialSelectionException;
import aros.services.rms.core.specialselection.domain.QuestionType;
import aros.services.rms.core.specialselection.domain.SpecialSelectionConfiguration;
import aros.services.rms.core.specialselection.domain.SpecialSelectionGroup;
import aros.services.rms.core.specialselection.domain.SpecialSelectionQuestion;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link SpecialSelectionValidator}. */
class SpecialSelectionValidatorTest {

  private SpecialSelectionValidator validator;

  @BeforeEach
  void setUp() {
    validator = new SpecialSelectionValidator();
  }

  private SpecialSelectionConfiguration buildConfig(List<SpecialSelectionQuestion> questions) {
    return SpecialSelectionConfiguration.builder()
        .name("Test Combo")
        .basePrice(10.0)
        .groups(
            List.of(
                SpecialSelectionGroup.builder()
                    .categoryId(1L)
                    .minSelections(1)
                    .maxSelections(1)
                    .required(true)
                    .productIds(List.of(100L))
                    .build()))
        .additions(List.of())
        .questions(questions != null ? questions : List.of())
        .build();
  }

  private SpecialSelectionQuestion buildQuestion(Long id, String text, QuestionType type) {
    return SpecialSelectionQuestion.builder()
        .id(id)
        .productId(1L)
        .question(text)
        .required(true)
        .displayOrder(0)
        .questionType(type)
        .build();
  }

  @Test
  void should_accept_text_answer_for_text_question() {
    var config = buildConfig(List.of(buildQuestion(1L, "Name?", QuestionType.TEXT)));
    var clarifications =
        List.of(ClarificationAnswer.builder().questionId(1L).answer("John").build());
    assertDoesNotThrow(
        () -> validator.validateOrderSelections(config, List.of(100L), List.of(), clarifications));
  }

  @Test
  void should_accept_boolean_true_answer() {
    var config = buildConfig(List.of(buildQuestion(1L, "Spicy?", QuestionType.BOOLEAN)));
    var clarifications =
        List.of(ClarificationAnswer.builder().questionId(1L).answer("true").build());
    assertDoesNotThrow(
        () -> validator.validateOrderSelections(config, List.of(100L), List.of(), clarifications));
  }

  @Test
  void should_accept_boolean_false_answer() {
    var config = buildConfig(List.of(buildQuestion(1L, "Spicy?", QuestionType.BOOLEAN)));
    var clarifications =
        List.of(ClarificationAnswer.builder().questionId(1L).answer("false").build());
    assertDoesNotThrow(
        () -> validator.validateOrderSelections(config, List.of(100L), List.of(), clarifications));
  }

  @Test
  void should_reject_boolean_with_invalid_answer() {
    var config = buildConfig(List.of(buildQuestion(1L, "Spicy?", QuestionType.BOOLEAN)));
    var clarifications =
        List.of(ClarificationAnswer.builder().questionId(1L).answer("maybe").build());
    var ex =
        assertThrows(
            InvalidSpecialSelectionException.class,
            () ->
                validator.validateOrderSelections(
                    config, List.of(100L), List.of(), clarifications));
    assertEquals(1, ex.getErrors().size());
    assertEquals("question 'Spicy?' requires a boolean answer (true/false)", ex.getErrors().get(0));
  }

  @Test
  void should_reject_required_text_question_when_blank() {
    var config = buildConfig(List.of(buildQuestion(1L, "Allergies?", QuestionType.TEXT)));
    var clarifications = List.of(ClarificationAnswer.builder().questionId(1L).answer("  ").build());
    var ex =
        assertThrows(
            InvalidSpecialSelectionException.class,
            () ->
                validator.validateOrderSelections(
                    config, List.of(100L), List.of(), clarifications));
    assertEquals(1, ex.getErrors().size());
    assertEquals("question 'Allergies?' is required but not answered", ex.getErrors().get(0));
  }

  @Test
  void should_accept_choice_answer_for_choice_question() {
    var config = buildConfig(List.of(buildQuestion(1L, "Size?", QuestionType.CHOICE)));
    var clarifications =
        List.of(ClarificationAnswer.builder().questionId(1L).answer("Large").build());
    assertDoesNotThrow(
        () -> validator.validateOrderSelections(config, List.of(100L), List.of(), clarifications));
  }

  @Test
  void should_reject_required_question_not_provided() {
    var config = buildConfig(List.of(buildQuestion(1L, "Name?", QuestionType.TEXT)));
    var ex =
        assertThrows(
            InvalidSpecialSelectionException.class,
            () -> validator.validateOrderSelections(config, List.of(100L), List.of(), List.of()));
    assertEquals(1, ex.getErrors().size());
    assertEquals("question 'Name?' is required but not answered", ex.getErrors().get(0));
  }

  @Test
  void should_handle_question_type_null_as_text() {
    var question =
        SpecialSelectionQuestion.builder()
            .id(1L)
            .productId(1L)
            .question("Legacy?")
            .required(true)
            .displayOrder(0)
            .questionType(null)
            .build();
    var config = buildConfig(List.of(question));
    var ex =
        assertThrows(
            InvalidSpecialSelectionException.class,
            () -> validator.validateOrderSelections(config, List.of(100L), List.of(), List.of()));
    assertEquals("question 'Legacy?' is required but not answered", ex.getErrors().get(0));
  }
}
