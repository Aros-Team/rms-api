package aros.services.rms.infraestructure.schedule.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** JPA entity for the time_logs table. */
@Entity
@Table(name = "time_logs")
public class TimeLogEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "worker_id", nullable = false)
  private Long workerId;

  @Column(nullable = false)
  private Instant timestamp;

  @Column(nullable = false)
  private String type;

  @Column(name = "within_shift", nullable = false)
  private boolean withinShift;

  @Column(name = "related_shift_id")
  private Long relatedShiftId;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getWorkerId() {
    return workerId;
  }

  public void setWorkerId(Long workerId) {
    this.workerId = workerId;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Instant timestamp) {
    this.timestamp = timestamp;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public boolean isWithinShift() {
    return withinShift;
  }

  public void setWithinShift(boolean withinShift) {
    this.withinShift = withinShift;
  }

  public Long getRelatedShiftId() {
    return relatedShiftId;
  }

  public void setRelatedShiftId(Long relatedShiftId) {
    this.relatedShiftId = relatedShiftId;
  }
}
