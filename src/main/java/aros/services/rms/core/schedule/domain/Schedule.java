package aros.services.rms.core.schedule.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Domain entity representing a work schedule with shifts. */
public class Schedule {
  private ScheduleId id;
  private String name;
  private String description;
  private List<ScheduleShift> shifts;

  /**
   * Creates a schedule with id, name, description and shifts.
   *
   * @param name schedule name, must not be blank
   * @param shifts list of shifts, must not be empty or overlapping
   * @param id schedule identifier, may be null for new schedules
   * @param description optional description
   */
  public Schedule(ScheduleId id, String name, String description, List<ScheduleShift> shifts) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("name must not be blank");
    }
    if (shifts == null || shifts.isEmpty()) {
      throw new IllegalArgumentException("schedule must have at least one shift");
    }
    validateNoOverlaps(shifts);
    this.id = id;
    this.name = name;
    this.description = description;
    this.shifts = new ArrayList<>(shifts);
  }

  /** Creates a new schedule without an id (id will be assigned by persistence). */
  public Schedule(String name, String description, List<ScheduleShift> shifts) {
    this(null, name, description, shifts);
  }

  /** Validates that no two shifts overlap in time. */
  private void validateNoOverlaps(List<ScheduleShift> shifts) {
    for (int i = 0; i < shifts.size(); i++) {
      for (int j = i + 1; j < shifts.size(); j++) {
        if (shifts.get(i).overlapsWith(shifts.get(j))) {
          throw new IllegalArgumentException(
              "shifts must not overlap: " + shifts.get(i) + " overlaps " + shifts.get(j));
        }
      }
    }
  }

  /** Adds a shift, validating it does not overlap existing ones. */
  public void addShift(ScheduleShift shift) {
    for (ScheduleShift existing : shifts) {
      if (existing.overlapsWith(shift)) {
        throw new IllegalArgumentException("shift overlaps with existing shift: " + existing);
      }
    }
    shifts.add(shift);
  }

  /** Removes a shift matching the given day and start time. */
  public void removeShift(DayOfWeek dayOfWeek, java.time.LocalTime startTime) {
    shifts.removeIf(s -> s.dayOfWeek() == dayOfWeek && s.startTime().equals(startTime));
  }

  /** Returns all shifts for a given day of the week. */
  public List<ScheduleShift> getShiftsForDay(DayOfWeek day) {
    return shifts.stream().filter(s -> s.dayOfWeek() == day).toList();
  }

  /** Returns an unmodifiable view of all shifts. */
  public List<ScheduleShift> getShifts() {
    return Collections.unmodifiableList(shifts);
  }

  /** Replaces all shifts, validating no overlaps in the new set. */
  public void setShifts(List<ScheduleShift> newShifts) {
    validateNoOverlaps(newShifts);
    this.shifts = new ArrayList<>(newShifts);
  }

  /** Returns the schedule identifier. */
  public ScheduleId getId() {
    return id;
  }

  /** Returns the schedule name. */
  public String getName() {
    return name;
  }

  /** Returns the schedule description. */
  public String getDescription() {
    return description;
  }

  /** Sets the schedule identifier. */
  public void setId(ScheduleId id) {
    this.id = id;
  }

  /** Sets the schedule name. */
  public void setName(String name) {
    this.name = name;
  }

  /** Sets the schedule description. */
  public void setDescription(String description) {
    this.description = description;
  }
}
