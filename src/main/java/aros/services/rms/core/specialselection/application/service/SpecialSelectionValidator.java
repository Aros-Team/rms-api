package aros.services.rms.core.specialselection.application.service;

import aros.services.rms.core.order.domain.ClarificationAnswer;
import aros.services.rms.core.specialselection.domain.SpecialSelectionAddition;
import aros.services.rms.core.specialselection.domain.SpecialSelectionConfiguration;
import aros.services.rms.core.specialselection.domain.SpecialSelectionGroup;
import aros.services.rms.core.specialselection.domain.SpecialSelectionQuestion;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Validates special selection configurations and the selections captured for an order. */
public class SpecialSelectionValidator {

  /**
   * Validates the integrity of a special selection configuration.
   *
   * @param config the configuration to validate
   * @throws aros.services.rms.core.specialselection.application.exception
   *     .InvalidSpecialSelectionException if any validation rule fails
   */
  public void validateConfiguration(SpecialSelectionConfiguration config) {
    List<String> errors = new ArrayList<>();
    if (config.getName() == null || config.getName().isBlank()) {
      errors.add("name is required");
    }
    if (config.getBasePrice() == null || config.getBasePrice() < 0) {
      errors.add("basePrice must be a non-negative value");
    }
    if (config.getGroups() == null || config.getGroups().isEmpty()) {
      errors.add("at least one group is required");
    } else {
      for (SpecialSelectionGroup group : config.getGroups()) {
        if (group.getCategoryId() == null) {
          errors.add("group must have a categoryId");
        }
        if (group.getMinSelections() < 1) {
          errors.add("group must have minSelections >= 1");
        }
        if (group.getMaxSelections() < group.getMinSelections()) {
          errors.add("group must have maxSelections >= minSelections");
        }
        if (group.getProductIds() == null || group.getProductIds().isEmpty()) {
          errors.add("group must have at least one product");
        }
      }
    }
    if (config.isSchedulingRequired()
        && (config.getSchedule() == null || config.getSchedule().isEmpty())) {
      errors.add("schedule entries are required when schedulingRequired is true");
    }
    if (config.getSchedule() != null) {
      for (var entry : config.getSchedule()) {
        if (entry.getStartTime() != null
            && entry.getEndTime() != null
            && !entry.getStartTime().isBefore(entry.getEndTime())) {
          errors.add("schedule entry has startTime >= endTime");
        }
      }
    }
    if (!errors.isEmpty()) {
      throw new aros.services.rms.core.specialselection.application.exception
          .InvalidSpecialSelectionException(errors);
    }
  }

  /**
   * Validates the selections captured for a special selection item within an order.
   *
   * @param config the special selection configuration
   * @param selectedProductIds the product identifiers selected from the combo groups
   * @param additionIds the addition identifiers selected for the item
   * @param clarifications the clarification answers for the item
   * @throws aros.services.rms.core.specialselection.application.exception
   *     .InvalidSpecialSelectionException if any validation rule fails
   */
  public void validateOrderSelections(
      SpecialSelectionConfiguration config,
      List<Long> selectedProductIds,
      List<Long> additionIds,
      List<ClarificationAnswer> clarifications) {
    List<String> errors = new ArrayList<>();

    Set<Long> chosenProductSet =
        selectedProductIds != null
            ? selectedProductIds.stream().collect(Collectors.toSet())
            : Set.of();

    Map<String, List<Long>> productIdsByGroupDesc =
        config.getGroups().stream()
            .collect(
                Collectors.toMap(
                    g -> "group_" + g.getId(),
                    g -> g.getProductIds() != null ? g.getProductIds() : List.of()));

    Set<Long> allValidProductIds =
        productIdsByGroupDesc.values().stream().flatMap(List::stream).collect(Collectors.toSet());

    for (Long chosenId : chosenProductSet) {
      if (!allValidProductIds.contains(chosenId)) {
        errors.add("product id=" + chosenId + " is not valid for this combo");
      }
    }

    for (SpecialSelectionGroup group : config.getGroups()) {
      List<Long> groupProductIds =
          group.getProductIds() != null ? group.getProductIds() : List.of();
      long selectedInGroup = chosenProductSet.stream().filter(groupProductIds::contains).count();
      if (group.isRequired() && selectedInGroup == 0) {
        errors.add(
            "group (categoryId="
                + group.getCategoryId()
                + ") is required but no product was selected");
      }
      if (selectedInGroup < group.getMinSelections()) {
        errors.add(
            "group (categoryId="
                + group.getCategoryId()
                + ") requires at least "
                + group.getMinSelections()
                + " selections, got "
                + selectedInGroup);
      }
      if (selectedInGroup > group.getMaxSelections()) {
        errors.add(
            "group (categoryId="
                + group.getCategoryId()
                + ") allows at most "
                + group.getMaxSelections()
                + " selections, got "
                + selectedInGroup);
      }
    }

    Set<Long> validAdditionIds =
        config.getAdditions() != null
            ? config.getAdditions().stream()
                .map(SpecialSelectionAddition::getId)
                .collect(Collectors.toSet())
            : Set.of();
    if (additionIds != null) {
      for (Long id : additionIds) {
        if (!validAdditionIds.contains(id)) {
          errors.add("addition id=" + id + " is not valid for this combo");
        }
      }
    }

    Set<Long> validQuestionIds =
        config.getQuestions() != null
            ? config.getQuestions().stream()
                .map(SpecialSelectionQuestion::getId)
                .collect(Collectors.toSet())
            : Set.of();
    if (clarifications != null) {
      for (ClarificationAnswer ca : clarifications) {
        if (!validQuestionIds.contains(ca.getQuestionId())) {
          errors.add("question id=" + ca.getQuestionId() + " is not valid for this combo");
        }
      }
      for (SpecialSelectionQuestion q : config.getQuestions()) {
        if (q.isRequired()) {
          boolean answered =
              clarifications.stream()
                  .anyMatch(
                      ca ->
                          ca.getQuestionId().equals(q.getId())
                              && ca.getAnswer() != null
                              && !ca.getAnswer().isBlank());
          if (!answered) {
            errors.add("question '" + q.getQuestion() + "' is required but not answered");
          }
        }
      }
    } else if (config.getQuestions() != null) {
      for (SpecialSelectionQuestion q : config.getQuestions()) {
        if (q.isRequired()) {
          errors.add("question '" + q.getQuestion() + "' is required but not answered");
        }
      }
    }

    if (!errors.isEmpty()) {
      throw new aros.services.rms.core.specialselection.application.exception
          .InvalidSpecialSelectionException(errors);
    }
  }
}
