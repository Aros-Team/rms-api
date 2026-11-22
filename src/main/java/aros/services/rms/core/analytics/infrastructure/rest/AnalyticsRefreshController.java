/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.rest;

import aros.services.rms.core.analytics.domain.port.in.RefreshAnalyticsUseCase;
import aros.services.rms.infraestructure.share.security.JustAdminUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for an explicit analytics refresh. */
@RestController
@RequestMapping("/api/v1/analytics/refresh")
@Tag(name = "Analytics")
@JustAdminUser
@RequiredArgsConstructor
public class AnalyticsRefreshController {

  private final RefreshAnalyticsUseCase refreshAnalyticsUseCase;

  /**
   * Recomputes the menu cache, prime-cost rollups and alerts on demand.
   *
   * @param months trailing months to refresh (1..24, default 13)
   * @return a summary of the work performed
   */
  @PostMapping
  @Operation(
      summary = "Refresh analytics",
      description =
          "Recomputes the menu-engineering cache, the daily-to-monthly prime cost rollups and the"
              + " variance alerts. Safe to call repeatedly.")
  @ApiResponse(responseCode = "200", description = "Analytics refreshed")
  @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
  @ApiResponse(responseCode = "403", description = "Authenticated user lacks ROLE_ADMIN")
  public Map<String, Object> refresh(@RequestParam(defaultValue = "13") int months) {
    return refreshAnalyticsUseCase.refresh(months);
  }
}
