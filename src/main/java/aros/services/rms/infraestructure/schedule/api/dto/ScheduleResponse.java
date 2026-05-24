package aros.services.rms.infraestructure.schedule.api.dto;

import aros.services.rms.core.schedule.domain.Schedule;
import java.util.List;

/** Response with schedule details including shifts. */
public record ScheduleResponse(
    Long id, String name, String description, List<ScheduleShiftResponse> shifts) {

  /** Builds a ScheduleResponse from a domain Schedule. */
  public static ScheduleResponse fromDomain(Schedule schedule) {
    return new ScheduleResponse(
        schedule.getId().value(),
        schedule.getName(),
        schedule.getDescription(),
        schedule.getShifts().stream().map(ScheduleShiftResponse::fromDomain).toList());
  }
}
