/* (C) 2026 */

package aros.services.rms.core.user.domain;

import java.time.Instant;

/** Domain entity representing a salary change history entry. Immutable once created. */
public class SalaryHistoryEntry {

  private final SalaryHistoryId id;
  private final UserId userId;
  private final Salary oldSalary;
  private final Salary newSalary;
  private final Instant changedAt;
  private final String reason;
  private final String observations;

  /**
   * Creates a salary history entry.
   *
   * @param id the entry identifier
   * @param userId the user identifier
   * @param oldSalary the previous salary (null for initial creation)
   * @param newSalary the new salary
   * @param changedAt the timestamp of the change
   * @param reason the reason for the change
   * @param observations additional observations
   */
  public SalaryHistoryEntry(
      SalaryHistoryId id,
      UserId userId,
      Salary oldSalary,
      Salary newSalary,
      Instant changedAt,
      String reason,
      String observations) {
    this.id = id;
    this.userId = userId;
    this.oldSalary = oldSalary;
    this.newSalary = newSalary;
    this.changedAt = changedAt;
    this.reason = reason;
    this.observations = observations;
  }

  /**
   * Gets the entry identifier.
   *
   * @return the id
   */
  public SalaryHistoryId getId() {
    return id;
  }

  /**
   * Gets the user identifier.
   *
   * @return the user id
   */
  public UserId getUserId() {
    return userId;
  }

  /**
   * Gets the previous salary.
   *
   * @return the old salary (null for initial creation)
   */
  public Salary getOldSalary() {
    return oldSalary;
  }

  /**
   * Gets the new salary.
   *
   * @return the new salary
   */
  public Salary getNewSalary() {
    return newSalary;
  }

  /**
   * Gets the timestamp of the change.
   *
   * @return the change timestamp
   */
  public Instant getChangedAt() {
    return changedAt;
  }

  /**
   * Gets the reason for the change.
   *
   * @return the reason
   */
  public String getReason() {
    return reason;
  }

  /**
   * Gets additional observations.
   *
   * @return the observations
   */
  public String getObservations() {
    return observations;
  }
}
