/* (C) 2026 */

package aros.services.rms.infraestructure.schedule.api.dto;

import aros.services.rms.core.schedule.port.input.AssignScheduleToWorkerUseCase.AssignmentResult;
import io.swagger.v3.oas.annotations.media.Schema;

/** Response returned after assigning a schedule to a worker. */
@Schema(
    description = "Result of assigning a schedule to a worker, including weekly hours and warnings")
public record ScheduleAssignmentResponse(
    @Schema(description = "Worker ID", example = "1001") Long workerId,
    @Schema(description = "Assigned schedule ID", example = "1") Long scheduleId,
    @Schema(description = "Worker's resulting weekly hours", example = "63.0") double weeklyHours,
    @Schema(description = "Legal weekly reference in hours", example = "48") int legalWeeklyHours,
    @Schema(description = "Hard weekly cap in hours", example = "66") int hardWeeklyCapHours,
    @Schema(
            description = "Non-blocking warning when hours exceed the legal reference",
            example = "Weekly hours 63.00 exceed the 48 h legal reference")
        String warning) {

  /**
   * Builds a response from an assignment result.
   *
   * @param result the assignment result
   * @return the response DTO
   */
  public static ScheduleAssignmentResponse fromResult(AssignmentResult result) {
    return new ScheduleAssignmentResponse(
        result.assignment().getWorkerId().value(),
        result.assignment().getScheduleId().value(),
        result.weeklyHours(),
        result.legalWeeklyHours(),
        result.hardWeeklyCapHours(),
        result.warning());
  }
}
