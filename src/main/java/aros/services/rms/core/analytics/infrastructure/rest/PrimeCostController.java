/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.rest;

import aros.services.rms.core.analytics.domain.port.in.GetPrimeCostUseCase;
import aros.services.rms.core.analytics.infrastructure.rest.dto.PrimeCostReportResponse;
import aros.services.rms.core.analytics.infrastructure.rest.mapper.PrimeCostReportMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for prime cost & margins analysis endpoints. */
@RestController
@RequestMapping("/api/v1/analytics/prime-cost")
@Tag(name = "Analytics")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class PrimeCostController {

  private final GetPrimeCostUseCase getPrimeCostUseCase;
  private final PrimeCostReportMapper mapper;

  /**
   * Returns the prime cost & margins report for the requested time bucket and period range.
   *
   * @param bucket the time bucket (daily, weekly, monthly, yearly)
   * @param from the inclusive start period key
   * @param to the inclusive end period key
   * @return the prime cost report
   */
  @GetMapping
  @Operation(
      summary = "Get prime cost & margins",
      description =
          "Returns a time series of prime cost and margin data for the requested "
              + "time bucket and period range. Pre-aggregated into monthly_financial_summary.")
  @ApiResponse(responseCode = "200", description = "Prime cost report retrieved")
  @ApiResponse(responseCode = "400", description = "Invalid period format or bucket")
  @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
  @ApiResponse(responseCode = "403", description = "Authenticated user lacks ROLE_ADMIN")
  @ApiResponse(responseCode = "404", description = "No data found for the requested range")
  @ApiResponse(responseCode = "422", description = "Invalid range (to < from or range > 366 days)")
  public PrimeCostReportResponse getPrimeCost(
      @RequestParam String bucket, @RequestParam String from, @RequestParam String to) {
    var report = getPrimeCostUseCase.execute(bucket, from, to);
    return mapper.toResponse(report);
  }
}
