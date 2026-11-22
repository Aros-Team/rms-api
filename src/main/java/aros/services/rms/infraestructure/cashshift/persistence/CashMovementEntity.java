/* (C) 2026 */

package aros.services.rms.infraestructure.cashshift.persistence;

import aros.services.rms.core.cashshift.domain.CashMovementType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** JPA entity for the {@code cash_movements} petty-cash ledger. */
@Entity
@Table(name = "cash_movements")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashMovementEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "shift_id", nullable = false)
  private Long shiftId;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private CashMovementType type;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal amount;

  @Column(nullable = false)
  private String reason;

  @Column(name = "created_by", nullable = false)
  private Long createdBy;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;
}
