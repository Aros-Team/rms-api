/* (C) 2026 */

package aros.services.rms.infraestructure.payroll.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** Entity representing a payroll record in the database. */
@Entity
@Table(name = "payroll")
public class PayrollEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "period_year", nullable = false)
  private int periodYear;

  @Column(name = "period_month", nullable = false)
  private int periodMonth;

  @Column(name = "period_start", nullable = false)
  private LocalDate periodStart;

  @Column(name = "period_end", nullable = false)
  private LocalDate periodEnd;

  @Column(name = "base_salary", nullable = false)
  private BigDecimal baseSalary;

  @Column(nullable = false)
  private BigDecimal bonuses;

  @Column(nullable = false)
  private BigDecimal deductions;

  @Column(name = "net_amount", nullable = false)
  private BigDecimal netAmount;

  @Column(name = "hours_worked", nullable = false)
  private BigDecimal hoursWorked;

  @Column(nullable = false)
  private String status;

  private String notes;

  @Column(name = "registered_by")
  private Long registeredBy;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

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

  public int getPeriodYear() {
    return periodYear;
  }

  public void setPeriodYear(int periodYear) {
    this.periodYear = periodYear;
  }

  public int getPeriodMonth() {
    return periodMonth;
  }

  public void setPeriodMonth(int periodMonth) {
    this.periodMonth = periodMonth;
  }

  public LocalDate getPeriodStart() {
    return periodStart;
  }

  public void setPeriodStart(LocalDate periodStart) {
    this.periodStart = periodStart;
  }

  public LocalDate getPeriodEnd() {
    return periodEnd;
  }

  public void setPeriodEnd(LocalDate periodEnd) {
    this.periodEnd = periodEnd;
  }

  public BigDecimal getBaseSalary() {
    return baseSalary;
  }

  public void setBaseSalary(BigDecimal baseSalary) {
    this.baseSalary = baseSalary;
  }

  public BigDecimal getBonuses() {
    return bonuses;
  }

  public void setBonuses(BigDecimal bonuses) {
    this.bonuses = bonuses;
  }

  public BigDecimal getDeductions() {
    return deductions;
  }

  public void setDeductions(BigDecimal deductions) {
    this.deductions = deductions;
  }

  public BigDecimal getNetAmount() {
    return netAmount;
  }

  public void setNetAmount(BigDecimal netAmount) {
    this.netAmount = netAmount;
  }

  public BigDecimal getHoursWorked() {
    return hoursWorked;
  }

  public void setHoursWorked(BigDecimal hoursWorked) {
    this.hoursWorked = hoursWorked;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public Long getRegisteredBy() {
    return registeredBy;
  }

  public void setRegisteredBy(Long registeredBy) {
    this.registeredBy = registeredBy;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}
