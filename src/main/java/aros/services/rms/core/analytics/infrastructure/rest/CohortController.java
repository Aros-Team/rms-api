/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.rest;

import aros.services.rms.core.analytics.domain.port.in.GetCohortUseCase;
import aros.services.rms.core.analytics.infrastructure.rest.dto.CohortReportResponse;
import aros.services.rms.core.analytics.infrastructure.rest.mapper.CohortReportMapper;
import aros.services.rms.infraestructure.share.security.JustAdminUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for the customer cohort report. */
@RestController
@RequestMapping("/api/v1/analytics/cohort")
@Tag(name = "Analytics")
@JustAdminUser
@RequiredArgsConstructor
public class CohortController {

  private final GetCohortUseCase getCohortUseCase;
  private final CohortReportMapper mapper;

  /**
   * Returns retention and ticket metrics for the requested range.
   *
   * @param bucket the time bucket
   * @param from the inclusive start period key
   * @param to the inclusive end period key
   * @return the cohort report
   */
  @GetMapping
  @Operation(
      summary = "Get customer cohorts",
      description =
          "Returns new and recurring visits, retention by bucket key, and average ticket per"
              + " order and per cover. A client is an order visit identified by fingerprint.")
  @ApiResponse(responseCode = "200", description = "Cohort report retrieved")
  @ApiResponse(responseCode = "400", description = "Invalid period format or bucket")
  @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
  @ApiResponse(responseCode = "403", description = "Authenticated user lacks ROLE_ADMIN")
  @ApiResponse(responseCode = "422", description = "Invalid range or range too large")
  public CohortReportResponse getCohort(
      @RequestParam String bucket, @RequestParam String from, @RequestParam String to) {
    return mapper.toResponse(getCohortUseCase.execute(bucket, from, to));
  }
}
