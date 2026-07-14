package aros.services.rms.infraestructure.specialselection.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** JPA entity representing a single weekly availability window for a special selection. */
@Entity
@Table(name = "special_selection_schedules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialSelectionScheduleEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "product_id", nullable = false)
  private Long productId;

  @Column(name = "day_of_week", nullable = false, length = 10)
  private String dayOfWeek;

  @Column(name = "start_time", nullable = false)
  private LocalTime startTime;

  @Column(name = "end_time", nullable = false)
  private LocalTime endTime;
}
