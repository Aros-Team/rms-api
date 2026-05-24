package aros.services.rms.infraestructure.schedule.api.dto;

import aros.services.rms.core.schedule.domain.DayOfWeek;
import aros.services.rms.core.schedule.port.input.GetWorkerShiftsUseCase.ShiftDetail;
import java.util.List;
import java.util.Map;

public record WorkerScheduleResponse(List<DayShiftsResponse> days) {

  public static WorkerScheduleResponse fromShiftMap(Map<DayOfWeek, List<ShiftDetail>> shifts) {
    List<DayShiftsResponse> days =
        shifts.entrySet().stream()
            .map(
                entry ->
                    new DayShiftsResponse(
                        entry.getKey().name(),
                        entry.getValue().stream()
                            .map(
                                d ->
                                    new ShiftDetailResponse(
                                        d.scheduleName(),
                                        d.startTime().toString(),
                                        d.endTime().toString()))
                            .toList()))
            .toList();
    return new WorkerScheduleResponse(days);
  }
}
