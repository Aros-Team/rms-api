package aros.services.rms.core.specialselection.application.service;

import aros.services.rms.core.product.domain.ProductOption;
import aros.services.rms.core.product.port.output.ProductOptionRepositoryPort;
import aros.services.rms.core.schedule.domain.DayOfWeek;
import aros.services.rms.core.specialselection.domain.SelectionType;
import aros.services.rms.core.specialselection.domain.SpecialSelectionAddition;
import aros.services.rms.core.specialselection.domain.SpecialSelectionConfiguration;
import aros.services.rms.core.specialselection.domain.SpecialSelectionGroup;
import aros.services.rms.core.specialselection.domain.SpecialSelectionQuestion;
import aros.services.rms.core.specialselection.domain.SpecialSelectionScheduleEntry;
import aros.services.rms.core.specialselection.domain.SpecialSelectionSnapshot;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Maps special selection configurations to and from serializable snapshots used for change history
 * persistence.
 */
public class SpecialSelectionSnapshotService {

  private final ObjectMapper objectMapper;
  private final ProductOptionRepositoryPort productOptionRepositoryPort;

  /**
   * Creates a new special selection snapshot service.
   *
   * @param productOptionRepositoryPort the product option repository port
   */
  public SpecialSelectionSnapshotService(ProductOptionRepositoryPort productOptionRepositoryPort) {
    this.productOptionRepositoryPort = productOptionRepositoryPort;
    this.objectMapper =
        new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  /**
   * Builds a snapshot from a configuration.
   *
   * @param config the configuration to snapshot
   * @return the snapshot, or null if the configuration is null
   */
  public SpecialSelectionSnapshot fromConfiguration(SpecialSelectionConfiguration config) {
    if (config == null) {
      return null;
    }
    return SpecialSelectionSnapshot.builder()
        .name(config.getName())
        .description(config.getDescription())
        .basePrice(config.getBasePrice())
        .selectionType(
            config.getSelectionType() != null ? config.getSelectionType().name() : "STANDARD")
        .baseRecipeEnabled(config.isBaseRecipeEnabled())
        .schedulingRequired(config.isSchedulingRequired())
        .active(config.isActive())
        .groups(
            config.getGroups() != null
                ? config.getGroups().stream()
                    .map(this::toGroupSnapshot)
                    .collect(Collectors.toList())
                : Collections.emptyList())
        .additions(
            config.getAdditions() != null
                ? config.getAdditions().stream()
                    .map(this::toAdditionSnapshot)
                    .collect(Collectors.toList())
                : Collections.emptyList())
        .questions(
            config.getQuestions() != null
                ? config.getQuestions().stream()
                    .map(this::toQuestionSnapshot)
                    .collect(Collectors.toList())
                : Collections.emptyList())
        .schedule(
            config.getSchedule() != null
                ? config.getSchedule().stream()
                    .map(this::toScheduleSnapshot)
                    .collect(Collectors.toList())
                : Collections.emptyList())
        .build();
  }

  /**
   * Serializes the snapshot to JSON.
   *
   * @param snapshot the snapshot to serialize
   * @return the JSON representation, or an empty object if the snapshot is null
   */
  public String toJson(SpecialSelectionSnapshot snapshot) {
    if (snapshot == null) {
      return "{}";
    }
    try {
      return objectMapper.writeValueAsString(snapshot);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to serialize special selection snapshot", e);
    }
  }

  /**
   * Deserializes a snapshot from JSON.
   *
   * @param json the JSON representation
   * @return the snapshot, or null if the input is blank
   */
  public SpecialSelectionSnapshot fromJson(String json) {
    if (json == null || json.isBlank()) {
      return null;
    }
    try {
      return objectMapper.readValue(json, SpecialSelectionSnapshot.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to deserialize special selection snapshot", e);
    }
  }

  /**
   * Builds a configuration from a snapshot, reloading referenced product options from the
   * repository.
   *
   * @param snapshot the snapshot to convert
   * @return the reconstructed configuration, or null if the snapshot is null
   */
  public SpecialSelectionConfiguration toConfiguration(SpecialSelectionSnapshot snapshot) {
    if (snapshot == null) {
      return null;
    }
    SpecialSelectionConfiguration config = new SpecialSelectionConfiguration();
    config.setName(snapshot.getName());
    config.setDescription(snapshot.getDescription());
    config.setBasePrice(snapshot.getBasePrice());
    if (snapshot.getSelectionType() != null) {
      try {
        config.setSelectionType(SelectionType.valueOf(snapshot.getSelectionType()));
      } catch (IllegalArgumentException e) {
        config.setSelectionType(SelectionType.SPECIAL_SELECTION);
      }
    } else {
      config.setSelectionType(SelectionType.SPECIAL_SELECTION);
    }
    config.setBaseRecipeEnabled(snapshot.isBaseRecipeEnabled());
    config.setSchedulingRequired(snapshot.isSchedulingRequired());
    config.setActive(snapshot.isActive());
    config.setGroups(toGroups(snapshot.getGroups()));
    config.setAdditions(toAdditions(snapshot.getAdditions()));
    config.setQuestions(toQuestions(snapshot.getQuestions()));
    config.setSchedule(toSchedule(snapshot.getSchedule()));
    return config;
  }

  private List<SpecialSelectionGroup> toGroups(
      List<SpecialSelectionSnapshot.GroupSnapshot> groupSnapshots) {
    if (groupSnapshots == null) {
      return Collections.emptyList();
    }
    Set<Long> allOptionIds =
        groupSnapshots.stream()
            .filter(g -> g.getOptions() != null)
            .flatMap(
                g ->
                    g.getOptions().stream()
                        .map(SpecialSelectionSnapshot.OptionInGroupSnapshot::getId))
            .collect(Collectors.toSet());
    Map<Long, ProductOption> optionMap =
        allOptionIds.isEmpty()
            ? Collections.emptyMap()
            : productOptionRepositoryPort.findAllById(new ArrayList<>(allOptionIds)).stream()
                .collect(Collectors.toMap(ProductOption::getId, o -> o));
    return groupSnapshots.stream()
        .map(
            gs -> {
              List<ProductOption> options =
                  gs.getOptions() != null
                      ? gs.getOptions().stream()
                          .map(os -> optionMap.get(os.getId()))
                          .filter(o -> o != null)
                          .collect(Collectors.toList())
                      : Collections.emptyList();
              return SpecialSelectionGroup.builder()
                  .id(gs.getId())
                  .name(gs.getName())
                  .displayOrder(gs.getDisplayOrder())
                  .required(gs.isRequired())
                  .minSelections(gs.getMinSelections())
                  .maxSelections(gs.getMaxSelections())
                  .options(options)
                  .build();
            })
        .collect(Collectors.toList());
  }

  private List<SpecialSelectionAddition> toAdditions(
      List<SpecialSelectionSnapshot.AdditionSnapshot> additionSnapshots) {
    if (additionSnapshots == null) {
      return Collections.emptyList();
    }
    return additionSnapshots.stream()
        .map(
            as ->
                SpecialSelectionAddition.builder()
                    .id(as.getId())
                    .name(as.getName())
                    .extraPrice(as.getExtraPrice())
                    .optionId(as.getOptionId())
                    .displayOrder(as.getDisplayOrder())
                    .build())
        .collect(Collectors.toList());
  }

  private List<SpecialSelectionQuestion> toQuestions(
      List<SpecialSelectionSnapshot.QuestionSnapshot> questionSnapshots) {
    if (questionSnapshots == null) {
      return Collections.emptyList();
    }
    return questionSnapshots.stream()
        .map(
            qs ->
                SpecialSelectionQuestion.builder()
                    .id(qs.getId())
                    .question(qs.getQuestion())
                    .required(qs.isRequired())
                    .displayOrder(qs.getDisplayOrder())
                    .build())
        .collect(Collectors.toList());
  }

  private List<SpecialSelectionScheduleEntry> toSchedule(
      List<SpecialSelectionSnapshot.ScheduleEntrySnapshot> scheduleSnapshots) {
    if (scheduleSnapshots == null) {
      return Collections.emptyList();
    }
    return scheduleSnapshots.stream()
        .filter(ss -> ss.getDayOfWeek() != null)
        .map(
            ss ->
                SpecialSelectionScheduleEntry.builder()
                    .id(ss.getId())
                    .dayOfWeek(DayOfWeek.valueOf(ss.getDayOfWeek()))
                    .startTime(ss.getStartTime())
                    .endTime(ss.getEndTime())
                    .build())
        .collect(Collectors.toList());
  }

  private SpecialSelectionSnapshot.GroupSnapshot toGroupSnapshot(SpecialSelectionGroup group) {
    return SpecialSelectionSnapshot.GroupSnapshot.builder()
        .id(group.getId())
        .name(group.getName())
        .required(group.isRequired())
        .minSelections(group.getMinSelections())
        .maxSelections(group.getMaxSelections())
        .displayOrder(group.getDisplayOrder())
        .options(
            group.getOptions() != null
                ? group.getOptions().stream()
                    .map(opt -> toOptionInGroupSnapshot(opt, group.getId()))
                    .collect(Collectors.toList())
                : Collections.emptyList())
        .build();
  }

  private SpecialSelectionSnapshot.OptionInGroupSnapshot toOptionInGroupSnapshot(
      ProductOption option, Long groupId) {
    return SpecialSelectionSnapshot.OptionInGroupSnapshot.builder()
        .id(option.getId())
        .name(option.getName())
        .extraPrice(0.0)
        .displayOrder(0)
        .build();
  }

  private SpecialSelectionSnapshot.AdditionSnapshot toAdditionSnapshot(
      SpecialSelectionAddition addition) {
    return SpecialSelectionSnapshot.AdditionSnapshot.builder()
        .id(addition.getId())
        .name(addition.getName())
        .extraPrice(addition.getExtraPrice())
        .optionId(addition.getOptionId())
        .displayOrder(addition.getDisplayOrder())
        .build();
  }

  private SpecialSelectionSnapshot.QuestionSnapshot toQuestionSnapshot(
      SpecialSelectionQuestion question) {
    return SpecialSelectionSnapshot.QuestionSnapshot.builder()
        .id(question.getId())
        .question(question.getQuestion())
        .required(question.isRequired())
        .displayOrder(question.getDisplayOrder())
        .build();
  }

  private SpecialSelectionSnapshot.ScheduleEntrySnapshot toScheduleSnapshot(
      SpecialSelectionScheduleEntry entry) {
    return SpecialSelectionSnapshot.ScheduleEntrySnapshot.builder()
        .id(entry.getId())
        .dayOfWeek(entry.getDayOfWeek() != null ? entry.getDayOfWeek().name() : null)
        .startTime(entry.getStartTime())
        .endTime(entry.getEndTime())
        .build();
  }
}
