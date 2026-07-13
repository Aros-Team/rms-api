package aros.services.rms.infraestructure.schedule.api;

import aros.services.rms.core.schedule.port.input.GetTimeLogHistoryUseCase;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.infraestructure.schedule.api.dto.TimeLogResponse;
import aros.services.rms.infraestructure.share.security.JustAdminUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for time log history queries. */
@RestController
@RequestMapping("/api/v1/admin/time-logs")
@RequiredArgsConstructor
@Tag(
    name = "Time Logs",
    description = "Operations for querying worker time log history with filters")
public class TimeLogController {

  private final GetTimeLogHistoryUseCase getTimeLogHistoryUseCase;

  /** Returns filtered time log entries. */
  @GetMapping
  @JustAdminUser
  @Operation(
      tags = {"Time Logs"},
      summary = "Get time log history",
      description =
          "Returns worker time log entries filtered by worker, date range, or whether they fall"
              + " within an assigned shift. Admin access only.")
  @ApiResponse(responseCode = "200", description = "Time logs retrieved")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @ApiResponse(responseCode = "403", description = "Forbidden")
  @ApiResponse(responseCode = "500", description = "Internal server error")
  public ResponseEntity<List<TimeLogResponse>> getTimeLogs(
      @Parameter(description = "Filter by worker ID", example = "1") @RequestParam(required = false)
          Long workerId,
      @Parameter(
              description = "Filter from this instant (ISO-8601)",
              example = "2026-01-01T00:00:00Z")
          @RequestParam(required = false)
          Instant from,
      @Parameter(
              description = "Filter up to this instant (ISO-8601)",
              example = "2026-12-31T23:59:59Z")
          @RequestParam(required = false)
          Instant to,
      @Parameter(
              description = "If true, only entries that fall within an assigned shift",
              example = "true")
          @RequestParam(required = false)
          Boolean withinShift) {
    var filter =
        new GetTimeLogHistoryUseCase.TimeLogFilter(
            workerId != null ? UserId.of(workerId) : null, from, to, withinShift);
    var logs =
        getTimeLogHistoryUseCase.getHistory(filter).stream()
            .map(TimeLogResponse::fromDomain)
            .toList();
    return ResponseEntity.ok(logs);
  }
}
