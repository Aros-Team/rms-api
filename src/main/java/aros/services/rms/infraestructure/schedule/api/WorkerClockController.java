/* (C) 2026 */

package aros.services.rms.infraestructure.schedule.api;

import aros.services.rms.core.schedule.port.input.ClockInUseCase;
import aros.services.rms.core.schedule.port.input.ClockOutUseCase;
import aros.services.rms.core.user.application.exception.UserNotFoundException;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import aros.services.rms.infraestructure.schedule.api.dto.TimeLogResponse;
import aros.services.rms.infraestructure.share.security.JustWorkerOrAdmin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for the authenticated worker's clock-in and clock-out.
 *
 * <p>The worker is always resolved from the access token; there is no worker ID parameter, so one
 * worker cannot clock for another. Admins are allowed by the security annotation but can only act
 * on their own account through this route.
 */
@RestController
@RequestMapping("/api/v1/workers/me")
@RequiredArgsConstructor
@Tag(name = "Workers", description = "Operations for workers to record their own attendance")
public class WorkerClockController {

  private final ClockInUseCase clockInUseCase;
  private final ClockOutUseCase clockOutUseCase;
  private final UserRepositoryPort userRepositoryPort;

  /** Opens the authenticated worker's attendance session. */
  @PostMapping("/clock-in")
  @JustWorkerOrAdmin
  @Operation(
      tags = {"Workers"},
      summary = "Clock in",
      description =
          "Creates an IN entry for the authenticated worker and validates the active shift. "
              + "Refuses a second open session.")
  @ApiResponse(responseCode = "201", description = "Clock-in recorded")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @ApiResponse(responseCode = "403", description = "Forbidden")
  @ApiResponse(responseCode = "404", description = "Authenticated user not found")
  @ApiResponse(responseCode = "409", description = "An open clock-in session already exists")
  @ApiResponse(responseCode = "500", description = "Internal server error")
  public ResponseEntity<TimeLogResponse> clockIn(@AuthenticationPrincipal Jwt jwt) {
    var entry = clockInUseCase.clockIn(resolveWorkerId(jwt));
    return ResponseEntity.status(HttpStatus.CREATED).body(TimeLogResponse.fromDomain(entry));
  }

  /** Closes the authenticated worker's open attendance session. */
  @PostMapping("/clock-out")
  @JustWorkerOrAdmin
  @Operation(
      tags = {"Workers"},
      summary = "Clock out",
      description =
          "Creates an OUT entry for the authenticated worker and validates the active shift. "
              + "Refuses when no session is open.")
  @ApiResponse(responseCode = "201", description = "Clock-out recorded")
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @ApiResponse(responseCode = "403", description = "Forbidden")
  @ApiResponse(responseCode = "404", description = "Authenticated user not found")
  @ApiResponse(responseCode = "409", description = "No open clock-in session exists")
  @ApiResponse(responseCode = "500", description = "Internal server error")
  public ResponseEntity<TimeLogResponse> clockOut(@AuthenticationPrincipal Jwt jwt) {
    var entry = clockOutUseCase.clockOut(resolveWorkerId(jwt));
    return ResponseEntity.status(HttpStatus.CREATED).body(TimeLogResponse.fromDomain(entry));
  }

  private UserId resolveWorkerId(Jwt jwt) {
    return userRepositoryPort
        .findByEmail(jwt.getSubject())
        .orElseThrow(() -> new UserNotFoundException("User not found: " + jwt.getSubject()))
        .getId();
  }
}
