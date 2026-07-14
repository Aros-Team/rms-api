package aros.services.rms.infraestructure.specialselection.api;

import aros.services.rms.core.specialselection.port.input.GetSpecialSelectionUseCase;
import aros.services.rms.infraestructure.specialselection.api.dto.SpecialSelectionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller exposing read-only endpoints for viewing special selection configurations. */
@RestController
@RequestMapping("/api/v1/special-selections")
@RequiredArgsConstructor
@Tag(
    name = "Special Selections",
    description = "Read-only endpoints for viewing special selection combos (waiter/kitchen)")
public class SpecialSelectionController {

  private final GetSpecialSelectionUseCase getUseCase;

  /**
   * Returns all special selections that are currently available based on their schedule.
   *
   * @return list of available special selections wrapped in a 200 response
   */
  @GetMapping("/available")
  @Operation(
      summary = "Get available special selections",
      description =
          "Returns special selections that are currently active and within their schedule")
  @ApiResponse(responseCode = "200", description = "Available special selections")
  public ResponseEntity<List<SpecialSelectionResponse>> findAvailable() {
    List<SpecialSelectionResponse> result =
        getUseCase.findAvailableNow().stream()
            .map(SpecialSelectionResponse::fromDomain)
            .collect(Collectors.toList());
    return ResponseEntity.ok(result);
  }

  /**
   * Returns the special selection configuration associated with the given product identifier.
   *
   * @param productId the product identifier
   * @return the configuration wrapped in a 200 response, or 404 if not found
   */
  @GetMapping("/{productId}")
  @Operation(
      summary = "Get a special selection",
      description = "Returns a special selection configuration by product ID")
  public ResponseEntity<SpecialSelectionResponse> findById(@PathVariable Long productId) {
    return getUseCase
        .findById(productId)
        .map(SpecialSelectionResponse::fromDomain)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }
}
