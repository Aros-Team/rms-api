/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.rest;

import aros.services.rms.core.analytics.domain.AlertSeverity;
import aros.services.rms.core.analytics.domain.AlertStatus;
import aros.services.rms.core.analytics.domain.exception.InvalidAnalyticsPeriodException;
import aros.services.rms.core.analytics.domain.port.in.GetAlertsUseCase;
import aros.services.rms.core.analytics.domain.port.in.MarkAlertReadUseCase;
import aros.services.rms.core.analytics.infrastructure.rest.dto.AlertsPageResponse;
import aros.services.rms.core.analytics.infrastructure.rest.mapper.AlertsPageMapper;
import aros.services.rms.infraestructure.share.security.JustAdminUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for variance alerts. */
@RestController
@RequestMapping("/api/v1/analytics/alerts")
@Tag(name = "Analytics")
@JustAdminUser
@RequiredArgsConstructor
public class AlertsController {

  private final GetAlertsUseCase getAlertsUseCase;
  private final MarkAlertReadUseCase markAlertReadUseCase;
  private final AlertsPageMapper mapper;

  /**
   * Returns a page of alerts.
   *
   * @param status optional status filter
   * @param severity optional severity filter
   * @param bucket optional period bucket filter
   * @param from optional inclusive period key
   * @param to optional inclusive period key
   * @param limit page size (max 100)
   * @param offset zero-based offset
   * @return the alert page
   */
  @GetMapping
  @Operation(
      summary = "List variance alerts",
      description = "Paged alert list ordered by detection time, newest first.")
  @ApiResponse(responseCode = "200", description = "Alert page retrieved")
  @ApiResponse(responseCode = "400", description = "Invalid filter")
  @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
  @ApiResponse(responseCode = "403", description = "Authenticated user lacks ROLE_ADMIN")
  public AlertsPageResponse list(
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String severity,
      @RequestParam(required = false) String bucket,
      @RequestParam(required = false) String from,
      @RequestParam(required = false) String to,
      @RequestParam(defaultValue = "50") int limit,
      @RequestParam(defaultValue = "0") int offset) {
    return mapper.toResponse(
        getAlertsUseCase.execute(
            parseStatus(status), parseSeverity(severity), bucket, from, to, limit, offset));
  }

  /**
   * Marks an alert as read. Idempotent.
   *
   * @param id the alert id
   * @return 204 No Content
   */
  @PatchMapping("/{id}/read")
  @Operation(summary = "Mark an alert as read", description = "Idempotent mark-read transition.")
  @ApiResponse(responseCode = "204", description = "Alert marked as read")
  @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
  @ApiResponse(responseCode = "403", description = "Authenticated user lacks ROLE_ADMIN")
  @ApiResponse(responseCode = "404", description = "Alert not found")
  public ResponseEntity<Void> markRead(@PathVariable Long id) {
    markAlertReadUseCase.markRead(id);
    return ResponseEntity.noContent().build();
  }

  private static AlertStatus parseStatus(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return AlertStatus.valueOf(value.trim().toUpperCase(java.util.Locale.ROOT));
    } catch (IllegalArgumentException exception) {
      throw new InvalidAnalyticsPeriodException(
          "Invalid status '" + value + "'. Must be OPEN, READ or DISMISSED.");
    }
  }

  private static AlertSeverity parseSeverity(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return AlertSeverity.valueOf(value.trim().toUpperCase(java.util.Locale.ROOT));
    } catch (IllegalArgumentException exception) {
      throw new InvalidAnalyticsPeriodException(
          "Invalid severity '" + value + "'. Must be RED, YELLOW or INFO.");
    }
  }
}
