package aros.services.rms.core.schedule.port.input;

import aros.services.rms.core.schedule.domain.ScheduleId;

/** Use case for deleting a schedule. */
public interface DeleteScheduleUseCase {
  /**
   * Deletes the schedule identified by the given id.
   *
   * @param id the identifier of the schedule to delete
   */
  void delete(ScheduleId id);
}
