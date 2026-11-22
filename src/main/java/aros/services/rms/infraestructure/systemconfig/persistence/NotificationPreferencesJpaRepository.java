/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** JPA repository for the notification preferences singleton. */
@Repository
public interface NotificationPreferencesJpaRepository
    extends JpaRepository<NotificationPreferencesEntity, Long> {}
