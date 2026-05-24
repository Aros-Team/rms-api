package aros.services.rms.infraestructure.schedule.api;

import aros.services.rms.core.schedule.domain.WorkerScheduleAssignmentId;
import aros.services.rms.core.schedule.port.input.AssignScheduleToWorkerUseCase;
import aros.services.rms.core.schedule.port.input.RemoveScheduleFromWorkerUseCase;
import aros.services.rms.core.schedule.port.output.WorkerScheduleAssignmentRepositoryPort;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.infraestructure.schedule.api.dto.AssignScheduleRequest;
import aros.services.rms.infraestructure.share.security.JustAdminUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for schedule-to-worker assignments. */
@RestController
@RequestMapping("/api/v1/workers/{workerId}/schedule-assignments")
@RequiredArgsConstructor
@Tag(name = "Worker Schedule Assignments", description = "Assign schedules to workers")
public class WorkerScheduleAssignmentController {

  private final AssignScheduleToWorkerUseCase assignScheduleUseCase;
  private final RemoveScheduleFromWorkerUseCase removeScheduleUseCase;
  private final WorkerScheduleAssignmentRepositoryPort assignmentRepository;

  /** Assigns a schedule to a worker. */
  @PostMapping
  @JustAdminUser
  @Operation(summary = "Assign schedule to worker", description = "Assigns a schedule to a worker")
  @ApiResponse(responseCode = "201", description = "Schedule assigned")
  @ApiResponse(responseCode = "409", description = "Shift overlap detected")
  public ResponseEntity<Void> assign(
      @PathVariable Long workerId, @Valid @RequestBody AssignScheduleRequest request) {
    var info =
        new AssignScheduleToWorkerUseCase.AssignInfo(UserId.of(workerId), request.scheduleId());
    assignScheduleUseCase.assign(info);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  /** Lists all schedule assignments for a worker. */
  @GetMapping
  @JustAdminUser
  @Operation(
      summary = "List assignments",
      description = "Lists all schedule assignments for a worker")
  public ResponseEntity<List<Long>> getAssignments(@PathVariable Long workerId) {
    List<Long> scheduleIds =
        assignmentRepository.findByWorkerId(UserId.of(workerId)).stream()
            .map(a -> a.getScheduleId().value())
            .toList();
    return ResponseEntity.ok(scheduleIds);
  }

  /** Removes a schedule assignment from a worker. */
  @DeleteMapping("/{assignmentId}")
  @JustAdminUser
  @Operation(
      summary = "Remove schedule assignment",
      description = "Removes a schedule from a worker")
  @ApiResponse(responseCode = "204", description = "Assignment removed")
  public ResponseEntity<Void> remove(@PathVariable Long assignmentId) {
    removeScheduleUseCase.remove(WorkerScheduleAssignmentId.of(assignmentId));
    return ResponseEntity.noContent().build();
  }
}
