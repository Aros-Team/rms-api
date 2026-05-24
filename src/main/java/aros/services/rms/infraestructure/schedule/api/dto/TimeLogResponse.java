package aros.services.rms.infraestructure.schedule.api.dto;

import aros.services.rms.core.schedule.domain.TimeLog;
import java.time.Instant;

public record TimeLogResponse(
    Long id,
    Long workerId,
    Instant timestamp,
    String type,
    boolean withinShift,
    Long relatedShiftId) {

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
