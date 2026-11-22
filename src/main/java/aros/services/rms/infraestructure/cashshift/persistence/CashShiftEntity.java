/* (C) 2026 */

package aros.services.rms.infraestructure.cashshift.persistence;

import aros.services.rms.core.cashshift.domain.CashShiftStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity for the {@code cash_shifts} table.
 *
 * <p>{@code openFlag} is a generated column ({@code 1} while OPEN, {@code NULL} otherwise) with a
 * unique index, which enforces one open shift in the database. It is read-only from JPA.
 */
@Entity
@Table(
    name = "cash_shifts",
    uniqueConstraints = @UniqueConstraint(name = "uk_cash_shifts_open", columnNames = "open_flag"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashShiftEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CashShiftStatus status;

  @Column(nullable = false, length = 3)
  private String currency;

  @Column(name = "opened_by", nullable = false)
  private Long openedBy;

  @Column(name = "opened_at", nullable = false)
  private LocalDateTime openedAt;

  @Column(name = "closed_by")
  private Long closedBy;

  @Column(name = "closed_at")
  private LocalDateTime closedAt;

  @Column(name = "opening_float", nullable = false, precision = 12, scale = 2)
  private BigDecimal openingFloat;

  @Column(name = "expected_cash", precision = 12, scale = 2)
  private BigDecimal expectedCash;

  @Column(name = "counted_cash", precision = 12, scale = 2)
  private BigDecimal countedCash;

  @Column(name = "difference_amount", precision = 12, scale = 2)
  private BigDecimal differenceAmount;

  @Column(length = 500)
  private String notes;

  @Column(name = "open_flag", insertable = false, updatable = false)
  private Integer openFlag;
}
