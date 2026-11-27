/* (C) 2026 */

package aros.services.rms.core.notification.infrastructure.persistence.entity;

import aros.services.rms.core.notification.domain.NotificationSeverity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** JPA entity for the user_notifications table. */
@Entity
@Table(name = "user_notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNotificationEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id")
  private Long userId;

  @Column(nullable = false, length = 50)
  private String code;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private NotificationSeverity severity;

  @Column(nullable = false, length = 120)
  private String title;

  @Column(nullable = false, length = 500)
  private String message;

  @Column(length = 200)
  private String route;

  @Column(name = "dedupe_key", length = 150, unique = true)
  private String dedupeKey;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "read_at")
  private Instant readAt;
}
