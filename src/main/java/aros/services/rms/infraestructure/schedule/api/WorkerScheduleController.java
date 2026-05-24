package aros.services.rms.infraestructure.schedule.api;

import aros.services.rms.core.schedule.port.input.GetWorkerShiftsUseCase;
import aros.services.rms.core.user.domain.UserEmail;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import aros.services.rms.infraestructure.schedule.api.dto.WorkerScheduleResponse;
import aros.services.rms.infraestructure.share.security.JustWorkerOrAdmin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/workers/me/schedule")
@RequiredArgsConstructor
@Tag(name = "My Schedule", description = "Worker's own schedule")
public class WorkerScheduleController {

  private final GetWorkerShiftsUseCase getWorkerShiftsUseCase;
  private final UserRepositoryPort userRepositoryPort;

  @GetMapping
  @JustWorkerOrAdmin
  @Operation(
      summary = "Get my schedule",
      description = "Returns the authenticated worker's schedule")
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
