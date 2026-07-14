package aros.services.rms.infraestructure.specialselection.api;

import aros.services.rms.core.schedule.domain.DayOfWeek;
import aros.services.rms.core.specialselection.domain.SpecialSelectionAddition;
import aros.services.rms.core.specialselection.domain.SpecialSelectionConfiguration;
import aros.services.rms.core.specialselection.domain.SpecialSelectionGroup;
import aros.services.rms.core.specialselection.domain.SpecialSelectionQuestion;
import aros.services.rms.core.specialselection.domain.SpecialSelectionScheduleEntry;
import aros.services.rms.core.specialselection.domain.SuggestedPrice;
import aros.services.rms.core.specialselection.port.input.CreateSpecialSelectionUseCase;
import aros.services.rms.core.specialselection.port.input.DeleteSpecialSelectionUseCase;
import aros.services.rms.core.specialselection.port.input.GetSpecialSelectionHistoryUseCase;
import aros.services.rms.core.specialselection.port.input.GetSpecialSelectionUseCase;
import aros.services.rms.core.specialselection.port.input.RevertSpecialSelectionUseCase;
import aros.services.rms.core.specialselection.port.input.SuggestSpecialSelectionPriceUseCase;
import aros.services.rms.core.specialselection.port.input.UpdateSpecialSelectionPriceUseCase;
import aros.services.rms.core.specialselection.port.input.UpdateSpecialSelectionScheduleUseCase;
import aros.services.rms.core.specialselection.port.input.UpdateSpecialSelectionUseCase;
import aros.services.rms.infraestructure.specialselection.api.dto.SpecialSelectionHistoryResponse;
import aros.services.rms.infraestructure.specialselection.api.dto.SpecialSelectionPricePatchRequest;
import aros.services.rms.infraestructure.specialselection.api.dto.SpecialSelectionRequest;
import aros.services.rms.infraestructure.specialselection.api.dto.SpecialSelectionResponse;
import aros.services.rms.infraestructure.specialselection.api.dto.SpecialSelectionSchedulePatchRequest;
import aros.services.rms.infraestructure.specialselection.api.dto.SuggestedPriceRequest;
import aros.services.rms.infraestructure.specialselection.api.dto.SuggestedPriceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing administrative endpoints for managing special selection combo
 * configurations.
 */
@RestController
@RequestMapping("/api/v1/admin/special-selections")
@RequiredArgsConstructor
@Tag(
    name = "Special Selections (Admin)",
    description = "Admin operations for managing special selection combo configurations")
public class SpecialSelectionAdminController {

  private final CreateSpecialSelectionUseCase createUseCase;
  private final UpdateSpecialSelectionUseCase updateUseCase;
  private final UpdateSpecialSelectionPriceUseCase updatePriceUseCase;
  private final UpdateSpecialSelectionScheduleUseCase updateScheduleUseCase;
  private final DeleteSpecialSelectionUseCase deleteUseCase;
  private final GetSpecialSelectionUseCase getUseCase;
  private final GetSpecialSelectionHistoryUseCase historyUseCase;
  private final RevertSpecialSelectionUseCase revertUseCase;
  private final SuggestSpecialSelectionPriceUseCase suggestPriceUseCase;
  private final SpecialSelectionNotificationService notificationService;

  /**
   * Creates a new special selection configuration.
   *
   * @param request the creation payload
   * @return the created special selection wrapped in a 200 response
   */
  @PostMapping
  @Operation(
      summary = "Create a special selection",
      description = "Creates a new special selection configuration linked to an existing product")
  @ApiResponse(responseCode = "200", description = "Special selection created")
  @ApiResponse(responseCode = "400", description = "Invalid configuration")
  public ResponseEntity<SpecialSelectionResponse> create(
      @Valid @RequestBody SpecialSelectionRequest request) {
    SpecialSelectionConfiguration config = toDomain(request, request.productId());
    SpecialSelectionConfiguration saved = createUseCase.execute(config, currentUser());
    SpecialSelectionResponse response = SpecialSelectionResponse.fromDomain(saved);
    notificationService.notifySpecialSelectionUpdated(response);
    return ResponseEntity.ok(response);
  }

  /**
   * Replaces the full configuration of an existing special selection.
   *
   * @param productId the product identifier
   * @param request the new configuration payload
   * @return the updated special selection wrapped in a 200 response
   */
  @PutMapping("/{productId}")
  @Operation(
      summary = "Update a special selection",
      description = "Replaces the full configuration graph")
  public ResponseEntity<SpecialSelectionResponse> update(
      @PathVariable Long productId, @Valid @RequestBody SpecialSelectionRequest request) {
    SpecialSelectionConfiguration config = toDomain(request, productId);
    SpecialSelectionConfiguration saved = updateUseCase.execute(productId, config, currentUser());
    SpecialSelectionResponse response = SpecialSelectionResponse.fromDomain(saved);
    notificationService.notifySpecialSelectionUpdated(response);
    return ResponseEntity.ok(response);
  }

  /**
   * Updates only the base price of an existing special selection.
   *
   * @param productId the product identifier
   * @param request the price patch payload
   * @return the updated special selection wrapped in a 200 response
   */
  @PutMapping("/{productId}/price")
  @Operation(
      summary = "Update base price only",
      description = "Updates only the base price, recording a PRICE_CHANGE history entry")
  public ResponseEntity<SpecialSelectionResponse> updatePrice(
      @PathVariable Long productId, @Valid @RequestBody SpecialSelectionPricePatchRequest request) {
    SpecialSelectionConfiguration saved =
        updatePriceUseCase.execute(productId, request.basePrice(), currentUser());
    SpecialSelectionResponse response = SpecialSelectionResponse.fromDomain(saved);
    notificationService.notifySpecialSelectionUpdated(response);
    return ResponseEntity.ok(response);
  }

  /**
   * Updates only the availability schedule of an existing special selection.
   *
   * @param productId the product identifier
   * @param request the schedule patch payload
   * @return the updated special selection wrapped in a 200 response
   */
  @PutMapping("/{productId}/schedule")
  @Operation(
      summary = "Update schedule only",
      description = "Updates only the availability schedule")
  public ResponseEntity<SpecialSelectionResponse> updateSchedule(
      @PathVariable Long productId,
      @Valid @RequestBody SpecialSelectionSchedulePatchRequest request) {
    List<SpecialSelectionScheduleEntry> schedule =
        request.schedule().stream()
            .map(
                s ->
                    SpecialSelectionScheduleEntry.builder()
                        .productId(productId)
                        .dayOfWeek(DayOfWeek.valueOf(s.dayOfWeek()))
                        .startTime(s.startTime())
                        .endTime(s.endTime())
                        .build())
            .collect(Collectors.toList());
    SpecialSelectionConfiguration saved =
        updateScheduleUseCase.execute(productId, schedule, currentUser());
    SpecialSelectionResponse response = SpecialSelectionResponse.fromDomain(saved);
    notificationService.notifySpecialSelectionUpdated(response);
    return ResponseEntity.ok(response);
  }

  /**
   * Soft-deletes (deactivates) the special selection for the given product identifier.
   *
   * @param productId the product identifier
   * @return empty 200 response on success
   */
  @DeleteMapping("/{productId}")
  @Operation(
      summary = "Delete (deactivate) a special selection",
      description = "Soft delete — sets active=false and records a DELETE history entry")
  public ResponseEntity<Void> delete(@PathVariable Long productId) {
    deleteUseCase.execute(productId, currentUser());
    return ResponseEntity.ok().build();
  }

  /**
   * Retrieves the full configuration of a special selection.
   *
   * @param productId the product identifier
   * @return the configuration wrapped in a 200 response
   */
  @GetMapping("/{productId}")
  @Operation(
      summary = "Get special selection configuration",
      description = "Returns the full configuration for a product")
  public ResponseEntity<SpecialSelectionResponse> get(@PathVariable Long productId) {
    SpecialSelectionConfiguration config =
        getUseCase
            .findById(productId)
            .orElseThrow(() -> new RuntimeException("Special selection not found: " + productId));
    return ResponseEntity.ok(SpecialSelectionResponse.fromDomain(config));
  }

  /**
   * Lists all special selection configurations.
   *
   * @return list of all configurations wrapped in a 200 response
   */
  @GetMapping
  @Operation(
      summary = "List all special selections",
      description = "Returns all special selection configurations")
  public ResponseEntity<List<SpecialSelectionResponse>> findAll() {
    List<SpecialSelectionConfiguration> configs = getUseCase.findAll();
    List<SpecialSelectionResponse> response =
        configs.stream().map(SpecialSelectionResponse::fromDomain).collect(Collectors.toList());
    return ResponseEntity.ok(response);
  }

  /**
   * Retrieves the paginated change history of a special selection.
   *
   * @param productId the product identifier
   * @param page the page index (default 0)
   * @param size the page size (default 10)
   * @return paginated history wrapped in a 200 response
   */
  @GetMapping("/{productId}/history")
  @Operation(
      summary = "Get change history",
      description = "Returns paginated history of changes for a special selection")
  public ResponseEntity<Page<SpecialSelectionHistoryResponse>> getHistory(
      @PathVariable Long productId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Page<SpecialSelectionHistoryResponse> result =
        historyUseCase
            .getHistory(productId, PageRequest.of(page, size))
            .map(SpecialSelectionHistoryResponse::fromDomain);
    return ResponseEntity.ok(result);
  }

  /**
   * Reverts the special selection to a previous history version.
   *
   * @param productId the product identifier
   * @param version the version to revert to
   * @return the reverted special selection wrapped in a 200 response
   */
  @PostMapping("/{productId}/revert/{version}")
  @Operation(
      summary = "Revert to a previous version",
      description = "Restores the configuration from a historical version")
  public ResponseEntity<SpecialSelectionResponse> revert(
      @PathVariable Long productId, @PathVariable int version) {
    SpecialSelectionConfiguration saved = revertUseCase.execute(productId, version, currentUser());
    SpecialSelectionResponse response = SpecialSelectionResponse.fromDomain(saved);
    notificationService.notifySpecialSelectionUpdated(response);
    return ResponseEntity.ok(response);
  }

  /**
   * Suggests a price for the special selection based on recipe costs and margin.
   *
   * @param productId the product identifier
   * @param request the margin request payload
   * @return the suggested price wrapped in a 200 response
   */
  @PostMapping("/{productId}/suggest-price")
  @Operation(
      summary = "Suggest a price",
      description = "Calculates suggested price based on recipe costs and margin")
  public ResponseEntity<SuggestedPriceResponse> suggestPrice(
      @PathVariable Long productId, @Valid @RequestBody SuggestedPriceRequest request) {
    SuggestedPrice price = suggestPriceUseCase.execute(productId, request.marginPercent());
    return ResponseEntity.ok(SuggestedPriceResponse.fromDomain(price));
  }

  private SpecialSelectionConfiguration toDomain(SpecialSelectionRequest request, Long productId) {
    List<SpecialSelectionGroup> groups =
        Optional.ofNullable(request.groups()).orElse(Collections.emptyList()).stream()
            .map(
                g ->
                    SpecialSelectionGroup.builder()
                        .id(g.id())
                        .productId(productId)
                        .name(g.name())
                        .displayOrder(g.displayOrder())
                        .required(g.required())
                        .minSelections(g.minSelections())
                        .maxSelections(g.maxSelections())
                        .options(
                            g.optionIds() != null
                                ? g.optionIds().stream()
                                    .map(
                                        id ->
                                            aros.services.rms.core.product.domain.ProductOption
                                                .builder()
                                                .id(id)
                                                .build())
                                    .collect(Collectors.toList())
                                : Collections.emptyList())
                        .build())
            .collect(Collectors.toList());

    List<SpecialSelectionAddition> additions =
        Optional.ofNullable(request.additions()).orElse(Collections.emptyList()).stream()
            .map(
                a ->
                    SpecialSelectionAddition.builder()
                        .productId(productId)
                        .optionId(a.optionId())
                        .name(a.name())
                        .extraPrice(a.extraPrice())
                        .displayOrder(a.displayOrder())
                        .build())
            .collect(Collectors.toList());

    List<SpecialSelectionQuestion> questions =
        Optional.ofNullable(request.questions()).orElse(Collections.emptyList()).stream()
            .map(
                q ->
                    SpecialSelectionQuestion.builder()
                        .productId(productId)
                        .question(q.question())
                        .required(q.required())
                        .displayOrder(q.displayOrder())
                        .build())
            .collect(Collectors.toList());

    List<SpecialSelectionScheduleEntry> schedule =
        Optional.ofNullable(request.schedule()).orElse(Collections.emptyList()).stream()
            .map(
                s ->
                    SpecialSelectionScheduleEntry.builder()
                        .productId(productId)
                        .dayOfWeek(DayOfWeek.valueOf(s.dayOfWeek()))
                        .startTime(s.startTime())
                        .endTime(s.endTime())
                        .build())
            .collect(Collectors.toList());

    return SpecialSelectionConfiguration.builder()
        .productId(productId)
        .name(request.name())
        .description(request.description())
        .basePrice(request.basePrice())
        .baseRecipeEnabled(request.baseRecipeEnabled())
        .schedulingRequired(request.schedulingRequired())
        .groups(groups)
        .additions(additions)
        .questions(questions)
        .schedule(schedule)
        .build();
  }

  private String currentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
      return "system";
    }
    return auth.getName();
  }
}
