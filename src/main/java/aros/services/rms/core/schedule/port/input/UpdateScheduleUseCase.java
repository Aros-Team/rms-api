package aros.services.rms.core.schedule.port.input;

import aros.services.rms.core.schedule.domain.Schedule;
import aros.services.rms.core.schedule.domain.ScheduleId;
import java.util.List;

/** Use case for updating an existing schedule. */
public interface UpdateScheduleUseCase {
  /**
   * Updates the schedule identified by the given id with the provided info.
   *
   * @param id the identifier of the schedule to update
   * @param info the updated schedule details
   * @return the updated schedule
   */
  Schedule update(ScheduleId id, UpdateScheduleInfo info);

  /**
   * Information required to update a schedule.
   *
   * @param name the schedule name
   * @param description the schedule description
   * @param shifts the updated shifts
   */
  record UpdateScheduleInfo(String name, String description, List<ShiftInfo> shifts) {
    /**
     * Information for a single shift within a schedule.
     *
     * @param dayOfWeek the day of the week (e.g. "MONDAY")
     * @param startTime the shift start time (e.g. "09:00")
     * @param endTime the shift end time (e.g. "17:00")
     */
    public record ShiftInfo(String dayOfWeek, String startTime, String endTime) {}
  }
}
