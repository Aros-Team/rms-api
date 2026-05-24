package aros.services.rms.core.schedule.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Schedule {
  private ScheduleId id;
  private String name;
  private String description;
  private List<ScheduleShift> shifts;

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

  public Schedule(String name, String description, List<ScheduleShift> shifts) {
    this(null, name, description, shifts);
  }

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

  public void addShift(ScheduleShift shift) {
    for (ScheduleShift existing : shifts) {
      if (existing.overlapsWith(shift)) {
        throw new IllegalArgumentException("shift overlaps with existing shift: " + existing);
      }
    }
    shifts.add(shift);
  }

  public void removeShift(DayOfWeek dayOfWeek, java.time.LocalTime startTime) {
    shifts.removeIf(s -> s.dayOfWeek() == dayOfWeek && s.startTime().equals(startTime));
  }

  public List<ScheduleShift> getShiftsForDay(DayOfWeek day) {
    return shifts.stream().filter(s -> s.dayOfWeek() == day).toList();
  }

  public List<ScheduleShift> getShifts() {
    return Collections.unmodifiableList(shifts);
  }

  public void setShifts(List<ScheduleShift> newShifts) {
    validateNoOverlaps(newShifts);
    this.shifts = new ArrayList<>(newShifts);
  }

  public ScheduleId getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public void setId(ScheduleId id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
