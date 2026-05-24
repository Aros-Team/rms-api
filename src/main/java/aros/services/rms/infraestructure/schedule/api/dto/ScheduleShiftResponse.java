package aros.services.rms.infraestructure.schedule.api.dto;

import aros.services.rms.core.schedule.domain.ScheduleShift;

public record ScheduleShiftResponse(Long id, String dayOfWeek, String startTime, String endTime) {

  public static ScheduleShiftResponse fromDomain(ScheduleShift shift) {
    return new ScheduleShiftResponse(
        shift.id(),
        shift.dayOfWeek().name(),
        shift.startTime().toString(),
        shift.endTime().toString());
  }
}
