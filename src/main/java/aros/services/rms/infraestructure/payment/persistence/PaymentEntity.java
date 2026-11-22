/* (C) 2026 */

package aros.services.rms.infraestructure.payment.persistence;

import aros.services.rms.core.payment.domain.DiscountType;
import aros.services.rms.core.payment.domain.PaymentStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity for the {@code payments} table.
 *
 * <p>{@code activeOrderId} is a generated column in MySQL: it is {@code NULL} for voided rows so
 * the unique index allows at most one active payment per order. It is read-only from JPA.
 */
@Entity
@Table(
    name = "payments",
    uniqueConstraints = {
      @UniqueConstraint(name = "uk_payments_active_order", columnNames = "active_order_id"),
      @UniqueConstraint(name = "uk_payments_idempotency", columnNames = "idempotency_key"),
      @UniqueConstraint(name = "uk_payments_receipt", columnNames = "receipt_number")
    })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "order_id", nullable = false)
  private Long orderId;

  @Column(name = "shift_id", nullable = false)
  private Long shiftId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentStatus status;

  @Column(nullable = false, length = 3)
  private String currency;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal subtotal;

  @Enumerated(EnumType.STRING)
  @Column(name = "discount_type")
  private DiscountType discountType;

  @Column(name = "discount_value", precision = 12, scale = 2)
  private BigDecimal discountValue;

  @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal discountAmount;

  @Column(name = "discount_reason")
  private String discountReason;

  @Column(name = "tip_amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal tipAmount;

  @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal totalAmount;

  @Column(name = "tendered_amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal tenderedAmount;

  @Column(name = "change_amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal changeAmount;

  @Column(name = "receipt_number")
  private String receiptNumber;

  @Column(name = "cashier_id", nullable = false)
  private Long cashierId;

  @Column(name = "idempotency_key", nullable = false, length = 100)
  private String idempotencyKey;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "completed_at")
  private LocalDateTime completedAt;

  @Column(name = "voided_at")
  private LocalDateTime voidedAt;

  @Column(name = "voided_by")
  private Long voidedBy;

  @Column(name = "void_reason")
  private String voidReason;

  @Version
  @Column(nullable = false)
  private Long version;

  @Column(name = "active_order_id", insertable = false, updatable = false)
  private Long activeOrderId;

  @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("sortOrder ASC")
  @Builder.Default
  private List<PaymentTenderEntity> tenders = new ArrayList<>();
}
