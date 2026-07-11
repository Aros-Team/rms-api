package aros.services.rms.core.schedule.port.input;

import aros.services.rms.core.schedule.domain.Schedule;
import java.util.List;

/** Use case for creating a new schedule. */
public interface CreateScheduleUseCase {
  /**
   * Creates a new schedule from the provided info.
   *
   * @param info the details for the new schedule
   * @return the created schedule
   */
  Schedule create(CreateScheduleInfo info);

  /**
   * Information required to create a schedule.
   *
   * @param name the schedule name
   * @param description the schedule description
   * @param shifts the shifts that compose the schedule
   */
  record CreateScheduleInfo(String name, String description, List<ShiftInfo> shifts) {
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
