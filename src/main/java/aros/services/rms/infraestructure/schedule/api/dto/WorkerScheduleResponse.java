package aros.services.rms.infraestructure.schedule.api.dto;

import aros.services.rms.core.schedule.domain.DayOfWeek;
import aros.services.rms.core.schedule.port.input.GetWorkerShiftsUseCase.ShiftDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;

/** Response with the worker's weekly schedule. */
@Schema(description = "Response DTO for the worker's weekly schedule grouped by day")
public record WorkerScheduleResponse(
    @Schema(description = "List of days and their assigned shifts") List<DayShiftsResponse> days) {

  /** Builds a WorkerScheduleResponse from a map of shifts grouped by day. */
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
