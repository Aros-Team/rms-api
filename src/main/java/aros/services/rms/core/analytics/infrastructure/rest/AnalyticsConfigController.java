/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.rest;

import aros.services.rms.core.analytics.domain.port.in.GetAnalyticsConfigUseCase;
import aros.services.rms.core.analytics.domain.port.in.UpdateAnalyticsConfigUseCase;
import aros.services.rms.core.analytics.domain.port.in.UpdateAnalyticsConfigUseCase.UpdateAnalyticsConfigCommand;
import aros.services.rms.core.analytics.infrastructure.rest.dto.AnalyticsConfigResponse;
import aros.services.rms.core.analytics.infrastructure.rest.dto.UpdateAnalyticsConfigRequest;
import aros.services.rms.core.analytics.infrastructure.rest.mapper.AnalyticsConfigResponseMapper;
import aros.services.rms.core.user.application.exception.UserNotFoundByEmailException;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for the singleton analytics configuration. */
@RestController
@RequestMapping("/api/v1/analytics/config")
@Tag(name = "Analytics")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AnalyticsConfigController {

  private final GetAnalyticsConfigUseCase getAnalyticsConfigUseCase;
  private final UpdateAnalyticsConfigUseCase updateAnalyticsConfigUseCase;
  private final AnalyticsConfigResponseMapper mapper;
  private final UserRepositoryPort userRepositoryPort;

  /** Returns the singleton analytics configuration. */
  @GetMapping
  @Operation(
      summary = "Get analytics configuration",
      description = "Returns operating hours and variance alert thresholds used by analytics.")
  @ApiResponse(responseCode = "200", description = "Analytics configuration retrieved")
  @ApiResponse(responseCode = "400", description = "Invalid request")
  @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
  @ApiResponse(responseCode = "403", description = "Authenticated user lacks ROLE_ADMIN")
  public AnalyticsConfigResponse get() {
    return mapper.toResponse(getAnalyticsConfigUseCase.get());
  }

  /** Updates the singleton analytics configuration for the authenticated administrator. */
  @PatchMapping
  @Operation(
      summary = "Update analytics configuration",
      description = "Updates operating hours and variance alert thresholds used by analytics.")
  @ApiResponse(responseCode = "200", description = "Analytics configuration updated")
  @ApiResponse(responseCode = "400", description = "Invalid configuration values")
  @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
  @ApiResponse(responseCode = "403", description = "Authenticated user lacks ROLE_ADMIN")
  public AnalyticsConfigResponse update(
      @Valid @RequestBody UpdateAnalyticsConfigRequest request, Authentication authentication) {
    UpdateAnalyticsConfigCommand mappedCommand = mapper.toCommand(request);
    UpdateAnalyticsConfigCommand command =
        new UpdateAnalyticsConfigCommand(
            mappedCommand.defaultOpen(),
            mappedCommand.defaultClose(),
            mappedCommand.lunchStart(),
            mappedCommand.lunchEnd(),
            mappedCommand.dinnerStart(),
            mappedCommand.dinnerEnd(),
            mappedCommand.foodCostDeviationPp(),
            mappedCommand.laborCostDeviationPp(),
            mappedCommand.salesDropYoyPct(),
            currentUserId(authentication));
    return mapper.toResponse(updateAnalyticsConfigUseCase.update(command));
  }

  private Long currentUserId(Authentication authentication) {
    return userRepositoryPort
        .findByEmail(authentication.getName())
        .orElseThrow(UserNotFoundByEmailException::new)
        .getId()
        .value();
  }
}
