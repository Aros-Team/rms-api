package aros.services.rms.infraestructure.schedule.api.dto;

import aros.services.rms.core.schedule.domain.TimeLog;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/** Response with time log entry details. */
@Schema(description = "Response DTO for a worker time log entry (clock-in, clock-out, break)")
public record TimeLogResponse(
    @Schema(description = "Time log ID", example = "100") Long id,
    @Schema(description = "Worker ID", example = "1") Long workerId,
    @Schema(description = "Timestamp of the event", example = "2026-07-13T08:00:00Z")
        Instant timestamp,
    @Schema(
            description = "Type of event (CLOCK_IN, CLOCK_OUT, BREAK_START, BREAK_END)",
            example = "CLOCK_IN")
        String type,
    @Schema(description = "Whether the event falls within an assigned shift", example = "true")
        boolean withinShift,
    @Schema(
            description = "Related shift ID if the event is linked to a specific shift",
            example = "10")
        Long relatedShiftId) {

  /** Builds a TimeLogResponse from a domain TimeLog. */
  public static TimeLogResponse fromDomain(TimeLog log) {
    return new TimeLogResponse(
        log.getId().value(),
        log.getWorkerId().value(),
        log.getTimestamp(),
        log.getType().name(),
        log.isWithinShift(),
        log.getRelatedShiftId());
  }
}
