/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.rest;

import aros.services.rms.core.analytics.domain.port.in.GetMenuEngineeringUseCase;
import aros.services.rms.core.analytics.infrastructure.rest.dto.MenuEngineeringReportResponse;
import aros.services.rms.core.analytics.infrastructure.rest.mapper.MenuEngineeringReportMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for menu engineering BCG analysis endpoints. */
@RestController
@RequestMapping("/api/v1/analytics/menu-engineering")
@Tag(name = "Analytics")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class MenuEngineeringController {

  private final GetMenuEngineeringUseCase getMenuEngineeringUseCase;
  private final MenuEngineeringReportMapper mapper;

  /**
   * Returns the menu engineering BCG report for the requested time bucket and period range.
   *
   * @param bucket the time bucket (daily, weekly, monthly, yearly)
   * @param from the inclusive start period key
   * @param to the inclusive end period key
   * @param categoryId optional category filter
   * @return the menu engineering report
   */
  @GetMapping
  @Operation(
      summary = "Get menu engineering BCG report",
      description =
          "Returns BCG quadrant analysis for menu items over the requested "
              + "time bucket and period range. Data is pre-computed into menu_performance_cache.")
  @ApiResponse(responseCode = "200", description = "Menu engineering report retrieved")
  @ApiResponse(responseCode = "400", description = "Invalid period format or bucket")
  @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
  @ApiResponse(responseCode = "403", description = "Authenticated user lacks ROLE_ADMIN")
  @ApiResponse(responseCode = "422", description = "Invalid range (to < from or range > 366 days)")
  public MenuEngineeringReportResponse getMenuEngineering(
      @RequestParam String bucket,
      @RequestParam String from,
      @RequestParam String to,
      @RequestParam(required = false) Long categoryId) {
    var report = getMenuEngineeringUseCase.execute(bucket, from, to, categoryId);
    return mapper.toResponse(report);
  }
}
