/* (C) 2026 */

package aros.services.rms.infraestructure.daymenu.api;

import aros.services.rms.core.daymenu.application.exception.UnauthenticatedOperationException;
import aros.services.rms.core.daymenu.port.input.GetCurrentDayMenuUseCase;
import aros.services.rms.core.daymenu.port.input.GetDayMenuHistoryUseCase;
import aros.services.rms.core.daymenu.port.input.UpdateDayMenuUseCase;
import aros.services.rms.infraestructure.daymenu.api.dto.DayMenuHistoryResponse;
import aros.services.rms.infraestructure.daymenu.api.dto.DayMenuResponse;
import aros.services.rms.infraestructure.daymenu.api.dto.UpdateDayMenuRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for day menu management. */
@RestController
@RequestMapping("/api/v1/day-menu")
@RequiredArgsConstructor
@Tag(name = "Day Menu", description = "Day menu and history management")
public class DayMenuController {

  private final UpdateDayMenuUseCase updateDayMenuUseCase;
  private final GetCurrentDayMenuUseCase getCurrentDayMenuUseCase;
  private final GetDayMenuHistoryUseCase getDayMenuHistoryUseCase;

  /**
   * Updates the day menu.
   *
   * @param request the update request
   * @return the updated day menu
   */
  @Operation(
      summary = "Update the day menu",
      description =
          "Sets a new product as the day menu. The product must have hasOptions=true. "
              + "The previous menu is automatically archived with its effective date.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Day menu updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid product or incorrect data"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "404", description = "Product not found")
      })
  @PutMapping
  public ResponseEntity<DayMenuResponse> update(@Valid @RequestBody UpdateDayMenuRequest request) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
      throw new UnauthenticatedOperationException();
    }
    String createdBy = auth.getName();
    var dayMenu = updateDayMenuUseCase.update(request.productId(), createdBy);
    return ResponseEntity.ok(DayMenuResponse.fromDomain(dayMenu));
  }

  /**
   * Gets the current day menu.
   *
   * @return the current day menu or no content
   */
  @Operation(
      summary = "Get the current day menu",
      description = "Returns the active day menu. If none is configured, returns 204 No Content.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Active day menu found"),
        @ApiResponse(responseCode = "204", description = "No day menu configured"),
        @ApiResponse(responseCode = "401", description = "User not authenticated")
      })
  @GetMapping("/current")
  public ResponseEntity<DayMenuResponse> getCurrent() {
    return getCurrentDayMenuUseCase
        .getCurrent()
        .map(DayMenuResponse::fromDomain)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.noContent().build());
  }

  /**
   * Gets the day menu history.
   *
   * @param page the page number
   * @param size the page size
   * @return the page of day menu history
   */
  @Operation(
      summary = "Get day menu history",
      description =
          "Returns the paginated history of previous day menus, ordered by effective date "
              + "descending. page >= 0, size between 1 and 100.",
      responses = {
        @ApiResponse(responseCode = "200", description = "History retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
        @ApiResponse(responseCode = "401", description = "User not authenticated")
      })
  @GetMapping("/history")
  public ResponseEntity<Page<DayMenuHistoryResponse>> getHistory(
      @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
    if (page < 0) {
      throw new IllegalArgumentException("Page parameter must be greater than or equal to 0");
    }
    if (size <= 0 || size > 100) {
      throw new IllegalArgumentException("Size parameter must be between 1 and 100");
    }
    var pageable = PageRequest.of(page, size);
    var result =
        getDayMenuHistoryUseCase.getHistory(pageable).map(DayMenuHistoryResponse::fromDomain);
    return ResponseEntity.ok(result);
  }
}
