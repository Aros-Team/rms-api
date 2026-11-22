/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Entity representing the single notification preferences row. */
@Entity
@Table(name = "notification_preferences")
public class NotificationPreferencesEntity {

  @Id private Long id;

  @Column(name = "kitchen_sound_enabled", nullable = false)
  private boolean kitchenSoundEnabled;

  @Column(name = "kitchen_haptics_enabled", nullable = false)
  private boolean kitchenHapticsEnabled;

  @Column(name = "low_stock_alerts_enabled", nullable = false)
  private boolean lowStockAlertsEnabled;

  @Column(name = "order_alerts_enabled", nullable = false)
  private boolean orderAlertsEnabled;

  @Column(name = "daily_summary_enabled", nullable = false)
  private boolean dailySummaryEnabled;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public boolean isKitchenSoundEnabled() {
    return kitchenSoundEnabled;
  }

  public void setKitchenSoundEnabled(boolean kitchenSoundEnabled) {
    this.kitchenSoundEnabled = kitchenSoundEnabled;
  }

  public boolean isKitchenHapticsEnabled() {
    return kitchenHapticsEnabled;
  }

  public void setKitchenHapticsEnabled(boolean kitchenHapticsEnabled) {
    this.kitchenHapticsEnabled = kitchenHapticsEnabled;
  }

  public boolean isLowStockAlertsEnabled() {
    return lowStockAlertsEnabled;
  }

  public void setLowStockAlertsEnabled(boolean lowStockAlertsEnabled) {
    this.lowStockAlertsEnabled = lowStockAlertsEnabled;
  }

  public boolean isOrderAlertsEnabled() {
    return orderAlertsEnabled;
  }

  public void setOrderAlertsEnabled(boolean orderAlertsEnabled) {
    this.orderAlertsEnabled = orderAlertsEnabled;
  }

  public boolean isDailySummaryEnabled() {
    return dailySummaryEnabled;
  }

  public void setDailySummaryEnabled(boolean dailySummaryEnabled) {
    this.dailySummaryEnabled = dailySummaryEnabled;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}
