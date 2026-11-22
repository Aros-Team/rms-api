/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.persistence;

import aros.services.rms.core.systemconfig.domain.NotificationSettings;
import aros.services.rms.core.systemconfig.domain.port.output.NotificationPreferencesPort;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Adapter for the global notification preferences singleton. */
@Component
@RequiredArgsConstructor
public class NotificationPreferencesAdapter implements NotificationPreferencesPort {

  private static final long SINGLETON_ID = 1L;

  private final NotificationPreferencesJpaRepository repository;

  /** {@inheritDoc} */
  @Override
  public Optional<NotificationSettings> find() {
    return repository
        .findById(SINGLETON_ID)
        .map(
            entity ->
                new NotificationSettings(
                    entity.isKitchenSoundEnabled(),
                    entity.isKitchenHapticsEnabled(),
                    entity.isLowStockAlertsEnabled(),
                    entity.isOrderAlertsEnabled(),
                    entity.isDailySummaryEnabled()));
  }

  /** {@inheritDoc} */
  @Override
  public NotificationSettings save(NotificationSettings settings) {
    NotificationPreferencesEntity entity =
        repository.findById(SINGLETON_ID).orElseGet(NotificationPreferencesEntity::new);
    entity.setId(SINGLETON_ID);
    entity.setKitchenSoundEnabled(settings.kitchenSoundEnabled());
    entity.setKitchenHapticsEnabled(settings.kitchenHapticsEnabled());
    entity.setLowStockAlertsEnabled(settings.lowStockAlertsEnabled());
    entity.setOrderAlertsEnabled(settings.orderAlertsEnabled());
    entity.setDailySummaryEnabled(settings.dailySummaryEnabled());
    entity.setUpdatedAt(Instant.now());
    repository.save(entity);
    return settings;
  }
}
