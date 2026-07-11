/* (C) 2026 */

package aros.services.rms.infraestructure.user.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

/** JPA entity for the salary_history table. */
@Entity
@Table(name = "salary_history")
public class SalaryHistoryEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "old_salary")
  private BigDecimal oldSalary;

  @Column(name = "new_salary", nullable = false)
  private BigDecimal newSalary;

  @Column(name = "changed_at", nullable = false)
  private Instant changedAt;

  @Column(nullable = false)
  private String reason;

  @Column(columnDefinition = "TEXT")
  private String observations;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public BigDecimal getOldSalary() {
    return oldSalary;
  }

  public void setOldSalary(BigDecimal oldSalary) {
    this.oldSalary = oldSalary;
  }

  public BigDecimal getNewSalary() {
    return newSalary;
  }

  public void setNewSalary(BigDecimal newSalary) {
    this.newSalary = newSalary;
  }

  public Instant getChangedAt() {
    return changedAt;
  }

  public void setChangedAt(Instant changedAt) {
    this.changedAt = changedAt;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public String getObservations() {
    return observations;
  }

  public void setObservations(String observations) {
    this.observations = observations;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }
}
