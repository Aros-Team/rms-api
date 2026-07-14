package aros.services.rms.infraestructure.specialselection.persistence.jpa;

import aros.services.rms.core.product.domain.ProductOption;
import aros.services.rms.core.schedule.domain.DayOfWeek;
import aros.services.rms.core.specialselection.domain.ChangeType;
import aros.services.rms.core.specialselection.domain.SpecialSelectionAddition;
import aros.services.rms.core.specialselection.domain.SpecialSelectionConfiguration;
import aros.services.rms.core.specialselection.domain.SpecialSelectionGroup;
import aros.services.rms.core.specialselection.domain.SpecialSelectionHistory;
import aros.services.rms.core.specialselection.domain.SpecialSelectionQuestion;
import aros.services.rms.core.specialselection.domain.SpecialSelectionScheduleEntry;
import aros.services.rms.infraestructure.product.persistence.Product;
import aros.services.rms.infraestructure.specialselection.persistence.ProductProductOptionEntity;
import aros.services.rms.infraestructure.specialselection.persistence.SpecialSelectionAdditionEntity;
import aros.services.rms.infraestructure.specialselection.persistence.SpecialSelectionGroupEntity;
import aros.services.rms.infraestructure.specialselection.persistence.SpecialSelectionHistoryEntity;
import aros.services.rms.infraestructure.specialselection.persistence.SpecialSelectionQuestionEntity;
import aros.services.rms.infraestructure.specialselection.persistence.SpecialSelectionScheduleEntity;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** Maps between special selection domain objects and their corresponding JPA entities. */
@Component
public class SpecialSelectionMapper {

  /**
   * Builds a special selection configuration from the product entity and its associated groups,
   * options, additions, questions and schedule entries.
   *
   * @param productEntity the product entity
   * @param groupEntities the option group entities
   * @param groupOptionLinks the links between groups and product options
   * @param additionEntities the addition entities
   * @param questionEntities the question entities
   * @param scheduleEntities the schedule entities
   * @param allOptions all referenced product options
   * @return the reconstructed configuration
   */
  public SpecialSelectionConfiguration toDomain(
      Product productEntity,
      List<SpecialSelectionGroupEntity> groupEntities,
      List<ProductProductOptionEntity> groupOptionLinks,
      List<SpecialSelectionAdditionEntity> additionEntities,
      List<SpecialSelectionQuestionEntity> questionEntities,
      List<SpecialSelectionScheduleEntity> scheduleEntities,
      List<ProductOption> allOptions) {

    Map<Long, ProductOption> optionMap =
        allOptions.stream().collect(Collectors.toMap(ProductOption::getId, o -> o));

    Map<Long, List<ProductProductOptionEntity>> optionLinksByGroup =
        groupOptionLinks.stream()
            .filter(l -> l.getSelectionGroupId() != null)
            .collect(Collectors.groupingBy(ProductProductOptionEntity::getSelectionGroupId));

    List<SpecialSelectionGroup> groups =
        groupEntities.stream()
            .map(
                ge -> {
                  List<ProductOption> groupOptions =
                      optionLinksByGroup.getOrDefault(ge.getId(), Collections.emptyList()).stream()
                          .map(link -> optionMap.get(link.getOptionId()))
                          .filter(o -> o != null)
                          .collect(Collectors.toList());
                  return SpecialSelectionGroup.builder()
                      .id(ge.getId())
                      .productId(ge.getProductId())
                      .name(ge.getName())
                      .displayOrder(ge.getDisplayOrder())
                      .required(ge.isRequired())
                      .minSelections(ge.getMinSelections())
                      .maxSelections(ge.getMaxSelections())
                      .options(groupOptions)
                      .build();
                })
            .collect(Collectors.toList());

    List<SpecialSelectionAddition> additions =
        additionEntities.stream()
            .map(
                ae ->
                    SpecialSelectionAddition.builder()
                        .id(ae.getId())
                        .productId(ae.getProductId())
                        .optionId(ae.getOptionId())
                        .option(optionMap.get(ae.getOptionId()))
                        .name(ae.getName())
                        .extraPrice(ae.getExtraPrice())
                        .displayOrder(ae.getDisplayOrder())
                        .build())
            .collect(Collectors.toList());

    List<SpecialSelectionQuestion> questions =
        questionEntities.stream()
            .map(
                qe ->
                    SpecialSelectionQuestion.builder()
                        .id(qe.getId())
                        .productId(qe.getProductId())
                        .question(qe.getQuestion())
                        .required(qe.isRequired())
                        .displayOrder(qe.getDisplayOrder())
                        .build())
            .collect(Collectors.toList());

    List<SpecialSelectionScheduleEntry> schedule =
        scheduleEntities.stream()
            .map(
                se -> {
                  DayOfWeek dow = null;
                  if (se.getDayOfWeek() != null) {
                    try {
                      dow = DayOfWeek.valueOf(se.getDayOfWeek());
                    } catch (IllegalArgumentException e) {
                      dow = null;
                    }
                  }
                  return SpecialSelectionScheduleEntry.builder()
                      .id(se.getId())
                      .productId(se.getProductId())
                      .dayOfWeek(dow)
                      .startTime(se.getStartTime())
                      .endTime(se.getEndTime())
                      .build();
                })
            .collect(Collectors.toList());

    return SpecialSelectionConfiguration.builder()
        .productId(productEntity.getId())
        .name(productEntity.getName())
        .description(productEntity.getDescription())
        .basePrice(productEntity.getBasePrice())
        .active(productEntity.isActive())
        .preparationAreaId(
            productEntity.getPreparationArea() != null
                ? productEntity.getPreparationArea().getId()
                : null)
        .selectionType(convertSelectionType(productEntity.getSelectionType()))
        .baseRecipeEnabled(productEntity.isSelectionBaseRecipeEnabled())
        .schedulingRequired(productEntity.isSchedulingRequired())
        .groups(groups)
        .additions(additions)
        .questions(questions)
        .schedule(schedule)
        .build();
  }

  /**
   * Converts a stored selection type string into its domain enum value, defaulting to STANDARD when
   * the value is missing or unknown.
   *
   * @param value the persisted selection type string
   * @return the resolved domain SelectionType
   */
  public aros.services.rms.core.specialselection.domain.SelectionType convertSelectionType(
      String value) {
    if (value == null) {
      return aros.services.rms.core.specialselection.domain.SelectionType.STANDARD;
    }
    try {
      return aros.services.rms.core.specialselection.domain.SelectionType.valueOf(value);
    } catch (IllegalArgumentException e) {
      return aros.services.rms.core.specialselection.domain.SelectionType.STANDARD;
    }
  }

  /**
   * Maps the given groups to their JPA entity representation for persistence.
   *
   * @param productId the product identifier
   * @param groups the domain groups
   * @return list of group entities (empty if the input is null)
   */
  public List<SpecialSelectionGroupEntity> toGroupEntities(
      Long productId, List<SpecialSelectionGroup> groups) {
    if (groups == null) {
      return Collections.emptyList();
    }
    return groups.stream()
        .map(
            g ->
                SpecialSelectionGroupEntity.builder()
                    .id(g.getId())
                    .productId(productId)
                    .name(g.getName())
                    .displayOrder(g.getDisplayOrder())
                    .required(g.isRequired())
                    .minSelections(g.getMinSelections())
                    .maxSelections(g.getMaxSelections())
                    .build())
        .collect(Collectors.toList());
  }

  /**
   * Maps the given groups to their product-option link entities, applying the provided group id
   * mapping for persistence.
   *
   * @param productId the product identifier
   * @param groups the domain groups
   * @param groupIdMapping the mapping from old group ids to newly persisted ids
   * @return list of product-option link entities
   */
  public List<ProductProductOptionEntity> toGroupOptionLinks(
      Long productId, List<SpecialSelectionGroup> groups, Map<Long, Long> groupIdMapping) {
    if (groups == null) {
      return Collections.emptyList();
    }
    return groups.stream()
        .flatMap(
            g -> {
              Long savedGroupId =
                  g.getId() != null ? groupIdMapping.getOrDefault(g.getId(), g.getId()) : null;
              if (savedGroupId == null || g.getOptions() == null) {
                return java.util.stream.Stream.empty();
              }
              return g.getOptions().stream()
                  .map(
                      opt ->
                          ProductProductOptionEntity.builder()
                              .productId(productId)
                              .optionId(opt.getId())
                              .selectionGroupId(savedGroupId)
                              .extraPrice(0.0)
                              .displayOrder(0)
                              .build());
            })
        .collect(Collectors.toList());
  }

  /**
   * Maps the given additions to their JPA entity representation.
   *
   * @param productId the product identifier
   * @param additions the domain additions
   * @return list of addition entities
   */
  public List<SpecialSelectionAdditionEntity> toAdditionEntities(
      Long productId, List<SpecialSelectionAddition> additions) {
    if (additions == null) {
      return Collections.emptyList();
    }
    return additions.stream()
        .map(
            a ->
                SpecialSelectionAdditionEntity.builder()
                    .id(a.getId())
                    .productId(productId)
                    .optionId(a.getOptionId())
                    .name(a.getName())
                    .extraPrice(a.getExtraPrice())
                    .displayOrder(a.getDisplayOrder())
                    .build())
        .collect(Collectors.toList());
  }

  /**
   * Maps the given questions to their JPA entity representation.
   *
   * @param productId the product identifier
   * @param questions the domain questions
   * @return list of question entities
   */
  public List<SpecialSelectionQuestionEntity> toQuestionEntities(
      Long productId, List<SpecialSelectionQuestion> questions) {
    if (questions == null) {
      return Collections.emptyList();
    }
    return questions.stream()
        .map(
            q ->
                SpecialSelectionQuestionEntity.builder()
                    .id(q.getId())
                    .productId(productId)
                    .question(q.getQuestion())
                    .required(q.isRequired())
                    .displayOrder(q.getDisplayOrder())
                    .build())
        .collect(Collectors.toList());
  }

  /**
   * Maps the given schedule entries to their JPA entity representation.
   *
   * @param productId the product identifier
   * @param schedule the domain schedule entries
   * @return list of schedule entities
   */
  public List<SpecialSelectionScheduleEntity> toScheduleEntities(
      Long productId, List<SpecialSelectionScheduleEntry> schedule) {
    if (schedule == null) {
      return Collections.emptyList();
    }
    return schedule.stream()
        .map(
            s ->
                SpecialSelectionScheduleEntity.builder()
                    .id(s.getId())
                    .productId(productId)
                    .dayOfWeek(s.getDayOfWeek() != null ? s.getDayOfWeek().name() : null)
                    .startTime(s.getStartTime())
                    .endTime(s.getEndTime())
                    .build())
        .collect(Collectors.toList());
  }

  /**
   * Maps a history entity to its domain representation.
   *
   * @param entity the history entity
   * @return the domain history entry, or null if the entity is null
   */
  public SpecialSelectionHistory toHistoryDomain(SpecialSelectionHistoryEntity entity) {
    if (entity == null) {
      return null;
    }
    ChangeType ct = null;
    if (entity.getChangeType() != null) {
      try {
        ct = ChangeType.valueOf(entity.getChangeType());
      } catch (IllegalArgumentException e) {
        ct = null;
      }
    }
    return SpecialSelectionHistory.builder()
        .id(entity.getId())
        .productId(entity.getProductId())
        .version(entity.getVersion())
        .changeType(ct)
        .snapshotJson(entity.getSnapshotJson())
        .changedBy(entity.getChangedBy())
        .changedAt(entity.getChangedAt())
        .isCurrent(entity.isCurrent())
        .build();
  }

  /**
   * Maps a domain history entry to its JPA entity representation.
   *
   * @param domain the domain history entry
   * @return the history entity, or null if the input is null
   */
  public SpecialSelectionHistoryEntity toHistoryEntity(SpecialSelectionHistory domain) {
    if (domain == null) {
      return null;
    }
    return SpecialSelectionHistoryEntity.builder()
        .id(domain.getId())
        .productId(domain.getProductId())
        .version(domain.getVersion())
        .changeType(domain.getChangeType() != null ? domain.getChangeType().name() : null)
        .snapshotJson(domain.getSnapshotJson())
        .changedBy(domain.getChangedBy())
        .changedAt(domain.getChangedAt())
        .isCurrent(domain.isCurrent())
        .build();
  }
}
