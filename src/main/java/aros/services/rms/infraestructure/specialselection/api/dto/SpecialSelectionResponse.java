package aros.services.rms.infraestructure.specialselection.api.dto;

import aros.services.rms.core.specialselection.domain.SpecialSelectionAddition;
import aros.services.rms.core.specialselection.domain.SpecialSelectionConfiguration;
import aros.services.rms.core.specialselection.domain.SpecialSelectionGroup;
import aros.services.rms.core.specialselection.domain.SpecialSelectionQuestion;
import aros.services.rms.core.specialselection.domain.SpecialSelectionScheduleEntry;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Response payload representing a special selection configuration including its nested groups,
 * additions, questions and schedule.
 */
@Schema(description = "Special selection configuration response")
public record SpecialSelectionResponse(
    @Schema(description = "Product ID", example = "1") Long productId,
    @Schema(description = "Display name", example = "Menú ejecutivo") String name,
    @Schema(description = "Description") String description,
    @Schema(description = "Base price", example = "12.99") Double basePrice,
    @Schema(description = "Whether active", example = "true") boolean active,
    @Schema(description = "Preparation area ID") Long preparationAreaId,
    @Schema(description = "Selection type", example = "SPECIAL_SELECTION") String selectionType,
    @Schema(description = "Base recipe cost enabled") boolean baseRecipeEnabled,
    @Schema(description = "Schedule required") boolean schedulingRequired,
    @Schema(description = "Product-category groups") List<GroupResponse> groups,
    @Schema(description = "Paid additions") List<AdditionResponse> additions,
    @Schema(description = "Clarification questions") List<QuestionResponse> questions,
    @Schema(description = "Availability schedule") List<ScheduleResponse> schedule) {

  /**
   * Maps a domain configuration to its API response representation.
   *
   * @param config the domain configuration
   * @return the API response, or null if the input is null
   */
  public static SpecialSelectionResponse fromDomain(SpecialSelectionConfiguration config) {
    if (config == null) {
      return null;
    }
    return new SpecialSelectionResponse(
        config.getProductId(),
        config.getName(),
        config.getDescription(),
        config.getBasePrice(),
        config.isActive(),
        config.getPreparationAreaId(),
        config.getSelectionType() != null ? config.getSelectionType().name() : "STANDARD",
        config.isBaseRecipeEnabled(),
        config.isSchedulingRequired(),
        config.getGroups() != null
            ? config.getGroups().stream()
                .map(GroupResponse::fromDomain)
                .collect(Collectors.toList())
            : Collections.emptyList(),
        config.getAdditions() != null
            ? config.getAdditions().stream()
                .map(AdditionResponse::fromDomain)
                .collect(Collectors.toList())
            : Collections.emptyList(),
        config.getQuestions() != null
            ? config.getQuestions().stream()
                .map(QuestionResponse::fromDomain)
                .collect(Collectors.toList())
            : Collections.emptyList(),
        config.getSchedule() != null
            ? config.getSchedule().stream()
                .map(ScheduleResponse::fromDomain)
                .collect(Collectors.toList())
            : Collections.emptyList());
  }

  /** Nested response payload for a product-category group inside a special selection. */
  @Schema(description = "Product-category group")
  public record GroupResponse(
      @Schema(description = "Group ID") Long id,
      @Schema(description = "Category ID") Long categoryId,
      @Schema(description = "Display order") int displayOrder,
      @Schema(description = "Whether required") boolean required,
      @Schema(description = "Minimum selections") int minSelections,
      @Schema(description = "Maximum selections") int maxSelections,
      @Schema(description = "Product IDs in this group") List<Long> productIds) {
    static GroupResponse fromDomain(SpecialSelectionGroup group) {
      return new GroupResponse(
          group.getId(),
          group.getCategoryId(),
          group.getDisplayOrder(),
          group.isRequired(),
          group.getMinSelections(),
          group.getMaxSelections(),
          group.getProductIds() != null ? group.getProductIds() : Collections.emptyList());
    }
  }

  /** Nested response payload for a paid addition inside a special selection. */
  @Schema(description = "Paid addition")
  public record AdditionResponse(
      @Schema(description = "Addition ID") Long id,
      @Schema(description = "Option ID") Long optionId,
      @Schema(description = "Display name") String name,
      @Schema(description = "Extra price") Double extraPrice,
      @Schema(description = "Display order") int displayOrder) {
    static AdditionResponse fromDomain(SpecialSelectionAddition addition) {
      return new AdditionResponse(
          addition.getId(),
          addition.getOptionId(),
          addition.getName(),
          addition.getExtraPrice(),
          addition.getDisplayOrder());
    }
  }

  /** Nested response payload for a clarification question inside a special selection. */
  @Schema(description = "Clarification question")
  public record QuestionResponse(
      @Schema(description = "Question ID") Long id,
      @Schema(description = "Question text") String question,
      @Schema(description = "Whether required") boolean required,
      @Schema(description = "Display order") int displayOrder) {
    static QuestionResponse fromDomain(SpecialSelectionQuestion question) {
      return new QuestionResponse(
          question.getId(),
          question.getQuestion(),
          question.isRequired(),
          question.getDisplayOrder());
    }
  }

  /** Nested response payload for an availability schedule entry inside a special selection. */
  @Schema(description = "Schedule entry")
  public record ScheduleResponse(
      @Schema(description = "Schedule ID") Long id,
      @Schema(description = "Day of week") String dayOfWeek,
      @Schema(description = "Start time") LocalTime startTime,
      @Schema(description = "End time") LocalTime endTime) {
    static ScheduleResponse fromDomain(SpecialSelectionScheduleEntry entry) {
      return new ScheduleResponse(
          entry.getId(),
          entry.getDayOfWeek() != null ? entry.getDayOfWeek().name() : null,
          entry.getStartTime(),
          entry.getEndTime());
    }
  }
}
