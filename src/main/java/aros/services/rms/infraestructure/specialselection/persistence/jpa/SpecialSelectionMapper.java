package aros.services.rms.infraestructure.specialselection.persistence.jpa;

import aros.services.rms.core.schedule.domain.DayOfWeek;
import aros.services.rms.core.specialselection.domain.ChangeType;
import aros.services.rms.core.specialselection.domain.SpecialSelectionAddition;
import aros.services.rms.core.specialselection.domain.SpecialSelectionConfiguration;
import aros.services.rms.core.specialselection.domain.SpecialSelectionGroup;
import aros.services.rms.core.specialselection.domain.SpecialSelectionHistory;
import aros.services.rms.core.specialselection.domain.SpecialSelectionQuestion;
import aros.services.rms.core.specialselection.domain.SpecialSelectionScheduleEntry;
import aros.services.rms.infraestructure.product.persistence.Product;
import aros.services.rms.infraestructure.specialselection.persistence.GroupProductEntity;
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

  /** Builds a domain configuration from persisted entities and group-product maps. */
  public SpecialSelectionConfiguration toDomain(
      Product productEntity,
      List<SpecialSelectionGroupEntity> groupEntities,
      Map<Long, List<Long>> groupProductMap,
      List<SpecialSelectionAdditionEntity> additionEntities,
      List<SpecialSelectionQuestionEntity> questionEntities,
      List<SpecialSelectionScheduleEntity> scheduleEntities) {
    List<SpecialSelectionGroup> groups =
        groupEntities.stream()
            .map(
                ge -> {
                  List<Long> productIds =
                      groupProductMap.getOrDefault(ge.getId(), Collections.emptyList());
                  return SpecialSelectionGroup.builder()
                      .id(ge.getId())
                      .displayOrder(ge.getDisplayOrder())
                      .required(ge.isRequired())
                      .minSelections(ge.getMinSelections())
                      .maxSelections(ge.getMaxSelections())
                      .categoryId(ge.getCategoryId())
                      .productIds(productIds)
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

  /** Converts a stored selection type string to its domain enum. */
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

  /** Converts domain groups to JPA entities for persisting. */
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
                    .categoryId(g.getCategoryId())
                    .displayOrder(g.getDisplayOrder())
                    .required(g.isRequired())
                    .minSelections(g.getMinSelections())
                    .maxSelections(g.getMaxSelections())
                    .build())
        .collect(Collectors.toList());
  }

  /** Converts domain group-product associations to JPA link entities. */
  public List<GroupProductEntity> toGroupProductLinks(
      Long productId, List<SpecialSelectionGroup> groups, Map<Long, Long> groupIdMapping) {
    if (groups == null) {
      return Collections.emptyList();
    }
    return groups.stream()
        .flatMap(
            g -> {
              Long savedGroupId =
                  g.getId() != null ? groupIdMapping.getOrDefault(g.getId(), g.getId()) : null;
              if (savedGroupId == null || g.getProductIds() == null) {
                return java.util.stream.Stream.empty();
              }
              return g.getProductIds().stream()
                  .map(
                      prodId ->
                          GroupProductEntity.builder()
                              .groupId(savedGroupId)
                              .productId(prodId)
                              .build());
            })
        .collect(Collectors.toList());
  }

  /** Converts domain additions to JPA entities for persisting. */
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

  /** Converts domain questions to JPA entities for persisting. */
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

  /** Converts domain schedule entries to JPA entities for persisting. */
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

  /** Converts a history entity to its domain representation. */
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

  /** Converts a domain history object to its JPA entity representation. */
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
