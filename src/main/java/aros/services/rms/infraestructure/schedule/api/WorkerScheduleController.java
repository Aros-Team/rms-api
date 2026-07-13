package aros.services.rms.infraestructure.schedule.api;

import aros.services.rms.core.schedule.port.input.GetWorkerShiftsUseCase;
import aros.services.rms.core.user.domain.UserEmail;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import aros.services.rms.infraestructure.schedule.api.dto.WorkerScheduleResponse;
import aros.services.rms.infraestructure.share.security.JustWorkerOrAdmin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for the authenticated worker's schedule. */
@RestController
@RequestMapping("/api/v1/workers/me/schedule")
@RequiredArgsConstructor
@Tag(name = "Workers", description = "Operations for workers to query their own assigned shifts")
public class WorkerScheduleController {

  private final GetWorkerShiftsUseCase getWorkerShiftsUseCase;
  private final UserRepositoryPort userRepositoryPort;

  /** Returns the authenticated worker's schedule. */
  @GetMapping
  @JustWorkerOrAdmin
  @Operation(
      tags = {"Workers"},
      summary = "Get my schedule",
      description = "Returns the authenticated worker's schedule grouped by day of week.")
  @ApiResponse(responseCode = "200", description = "Schedule retrieved")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @ApiResponse(responseCode = "403", description = "Forbidden")
  @ApiResponse(responseCode = "500", description = "Internal server error")
  public ResponseEntity<WorkerScheduleResponse> getMySchedule(@AuthenticationPrincipal Jwt jwt) {
    var userEmail = new UserEmail(jwt.getSubject());
    var user =
        userRepositoryPort
            .findByEmail(userEmail.value())
            .orElseThrow(() -> new RuntimeException("User not found"));
    var shifts = getWorkerShiftsUseCase.getShifts(user.getId());
    return ResponseEntity.ok(WorkerScheduleResponse.fromShiftMap(shifts));
  }
}
