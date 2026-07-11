package aros.services.rms.infraestructure.schedule.api;

import aros.services.rms.core.schedule.port.input.GetTimeLogHistoryUseCase;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.infraestructure.schedule.api.dto.TimeLogResponse;
import aros.services.rms.infraestructure.share.security.JustAdminUser;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Time Logs", description = "Worker time log history")
public class TimeLogController {

  private final GetTimeLogHistoryUseCase getTimeLogHistoryUseCase;

  /** Returns filtered time log entries. */
  @GetMapping
  @JustAdminUser
  @Operation(summary = "Get time log history", description = "Returns filtered time log entries")
  public ResponseEntity<List<TimeLogResponse>> getTimeLogs(
      @RequestParam(required = false) Long workerId,
      @RequestParam(required = false) Instant from,
      @RequestParam(required = false) Instant to,
      @RequestParam(required = false) Boolean withinShift) {
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
