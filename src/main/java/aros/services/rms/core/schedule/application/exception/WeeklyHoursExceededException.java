/* (C) 2026 */

package aros.services.rms.core.schedule.application.exception;

/** Thrown when an assignment would exceed the hard weekly hours cap. */
public class WeeklyHoursExceededException extends RuntimeException {
  private final double weeklyHours;
  private final int hardWeeklyCapHours;

  /**
   * Creates a new WeeklyHoursExceededException.
   *
   * @param message the detail message
   * @param weeklyHours the resulting weekly hours
   * @param hardWeeklyCapHours the hard cap that was exceeded
   */
  public WeeklyHoursExceededException(String message, double weeklyHours, int hardWeeklyCapHours) {
    super(message);
    this.weeklyHours = weeklyHours;
    this.hardWeeklyCapHours = hardWeeklyCapHours;
  }

  /**
   * Returns the resulting weekly hours.
   *
   * @return weekly hours after the attempted assignment
   */
  public double getWeeklyHours() {
    return weeklyHours;
  }

  /**
   * Returns the hard weekly cap.
   *
   * @return hard weekly cap in hours
   */
  public int getHardWeeklyCapHours() {
    return hardWeeklyCapHours;
  }
}
