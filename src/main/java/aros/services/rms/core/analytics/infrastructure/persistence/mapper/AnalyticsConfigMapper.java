/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.persistence.mapper;

import aros.services.rms.core.analytics.domain.AnalyticsConfig;
import aros.services.rms.core.analytics.infrastructure.persistence.entity.AnalyticsConfigEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/** MapStruct mapper between analytics configuration persistence and domain models. */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AnalyticsConfigMapper {

  /** Maps a persistence entity to the analytics configuration domain record. */
  AnalyticsConfig toDomain(AnalyticsConfigEntity entity);

  /** Maps an analytics configuration domain record to a persistence entity. */
  AnalyticsConfigEntity toEntity(AnalyticsConfig config);
}
