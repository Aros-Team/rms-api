/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.rest;

import aros.services.rms.core.analytics.domain.port.in.GetOperationsUseCase;
import aros.services.rms.core.analytics.infrastructure.rest.dto.OperationsReportResponse;
import aros.services.rms.core.analytics.infrastructure.rest.mapper.OperationsReportMapper;
import aros.services.rms.infraestructure.share.security.JustAdminUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for the operational efficiency report. */
@RestController
@RequestMapping("/api/v1/analytics/operations")
@Tag(name = "Analytics")
@JustAdminUser
@RequiredArgsConstructor
public class OperationsController {

  private final GetOperationsUseCase getOperationsUseCase;
  private final OperationsReportMapper mapper;

  /**
   * Returns RevPASH, covers, table turns and labor share for the requested range.
   *
   * @param bucket the time bucket
   * @param from the inclusive start period key
   * @param to the inclusive end period key
   * @return the operations report
   */
  @GetMapping
  @Operation(
      summary = "Get operational efficiency",
      description =
          "Returns RevPASH, covers, table turnover and labor share computed from orders, tables"
              + " and the daily financial rollups.")
  @ApiResponse(responseCode = "200", description = "Operations report retrieved")
  @ApiResponse(responseCode = "400", description = "Invalid period format or bucket")
  @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
  @ApiResponse(responseCode = "403", description = "Authenticated user lacks ROLE_ADMIN")
  @ApiResponse(responseCode = "422", description = "Invalid range or range too large")
  public OperationsReportResponse getOperations(
      @RequestParam String bucket, @RequestParam String from, @RequestParam String to) {
    return mapper.toResponse(getOperationsUseCase.execute(bucket, from, to));
  }
}
