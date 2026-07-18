/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.persistence.repository;

import aros.services.rms.core.analytics.infrastructure.persistence.entity.AnalyticsConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository for the singleton analytics configuration entity. */
public interface JpaAnalyticsConfigRepository
    extends JpaRepository<AnalyticsConfigEntity, Integer> {}
