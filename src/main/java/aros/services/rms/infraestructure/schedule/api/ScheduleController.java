package aros.services.rms.infraestructure.schedule.api;

import aros.services.rms.core.schedule.domain.Schedule;
import aros.services.rms.core.schedule.domain.ScheduleId;
import aros.services.rms.core.schedule.port.input.CreateScheduleUseCase;
import aros.services.rms.core.schedule.port.input.DeleteScheduleUseCase;
import aros.services.rms.core.schedule.port.input.UpdateScheduleUseCase;
import aros.services.rms.core.schedule.port.output.ScheduleRepositoryPort;
import aros.services.rms.infraestructure.schedule.api.dto.ScheduleRequest;
import aros.services.rms.infraestructure.schedule.api.dto.ScheduleResponse;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for schedule CRUD operations. */
@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
@Tag(name = "Schedules", description = "Schedule management for worker shifts")
public class ScheduleController {

  private final CreateScheduleUseCase createScheduleUseCase;
  private final UpdateScheduleUseCase updateScheduleUseCase;
  private final DeleteScheduleUseCase deleteScheduleUseCase;
  private final ScheduleRepositoryPort scheduleRepository;

  /** Creates a new schedule with shifts. */
  @PostMapping
  @JustAdminUser
  @Operation(summary = "Create a schedule", description = "Creates a new schedule with shifts")
  @ApiResponse(responseCode = "201", description = "Schedule created")
  @ApiResponse(responseCode = "400", description = "Invalid input")
  @ApiResponse(responseCode = "409", description = "Schedule name already exists")
  public ResponseEntity<ScheduleResponse> create(@Valid @RequestBody ScheduleRequest request) {
    var info =
        new CreateScheduleUseCase.CreateScheduleInfo(
            request.name(),
            request.description(),
            request.shifts().stream()
                .map(
                    s ->
                        new CreateScheduleUseCase.CreateScheduleInfo.ShiftInfo(
                            s.dayOfWeek(), s.startTime(), s.endTime()))
                .toList());
    Schedule schedule = createScheduleUseCase.create(info);
    return ResponseEntity.status(HttpStatus.CREATED).body(ScheduleResponse.fromDomain(schedule));
  }

  /** Returns all schedules. */
  @GetMapping
  @JustAdminUser
  @Operation(summary = "List all schedules", description = "Returns all schedules")
  public ResponseEntity<List<ScheduleResponse>> getAll() {
    List<ScheduleResponse> schedules =
        scheduleRepository.findAll().stream().map(ScheduleResponse::fromDomain).toList();
    return ResponseEntity.ok(schedules);
  }

  /** Returns a single schedule by ID. */
  @GetMapping("/{id}")
  @JustAdminUser
  @Operation(summary = "Get schedule by ID", description = "Returns a single schedule with shifts")
  @ApiResponse(responseCode = "200", description = "Schedule found")
  @ApiResponse(responseCode = "404", description = "Schedule not found")
  public ResponseEntity<ScheduleResponse> getById(@PathVariable Long id) {
    return scheduleRepository
        .findById(new ScheduleId(id))
        .map(s -> ResponseEntity.ok(ScheduleResponse.fromDomain(s)))
        .orElse(ResponseEntity.notFound().build());
  }

  /** Updates an existing schedule. */
  @PutMapping("/{id}")
  @JustAdminUser
  @Operation(summary = "Update a schedule", description = "Updates an existing schedule")
  @ApiResponse(responseCode = "200", description = "Schedule updated")
  @ApiResponse(responseCode = "404", description = "Schedule not found")
  public ResponseEntity<ScheduleResponse> update(
      @PathVariable Long id, @Valid @RequestBody ScheduleRequest request) {
    var info =
        new UpdateScheduleUseCase.UpdateScheduleInfo(
            request.name(),
            request.description(),
            request.shifts().stream()
                .map(
                    s ->
                        new UpdateScheduleUseCase.UpdateScheduleInfo.ShiftInfo(
                            s.dayOfWeek(), s.startTime(), s.endTime()))
                .toList());
    Schedule schedule = updateScheduleUseCase.update(new ScheduleId(id), info);
    return ResponseEntity.ok(ScheduleResponse.fromDomain(schedule));
  }

  /** Deletes a schedule if it has no active assignments. */
  @DeleteMapping("/{id}")
  @JustAdminUser
  @Operation(
      summary = "Delete a schedule",
      description = "Deletes a schedule if it has no assignments")
  @ApiResponse(responseCode = "204", description = "Schedule deleted")
  @ApiResponse(responseCode = "409", description = "Schedule has active assignments")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    deleteScheduleUseCase.delete(new ScheduleId(id));
    return ResponseEntity.noContent().build();
  }
}
