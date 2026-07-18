/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.rest.mapper;

import aros.services.rms.core.analytics.domain.AnalyticsConfig;
import aros.services.rms.core.analytics.domain.port.in.UpdateAnalyticsConfigUseCase.UpdateAnalyticsConfigCommand;
import aros.services.rms.core.analytics.infrastructure.rest.dto.AnalyticsConfigResponse;
import aros.services.rms.core.analytics.infrastructure.rest.dto.UpdateAnalyticsConfigRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/** MapStruct mapper for analytics configuration REST DTOs. */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AnalyticsConfigResponseMapper {

  /** Maps an analytics configuration domain record to its response DTO. */
  AnalyticsConfigResponse toResponse(AnalyticsConfig domain);

  /** Maps an update request to an application command without authentication metadata. */
  @Mapping(target = "updatedBy", ignore = true)
  UpdateAnalyticsConfigCommand toCommand(UpdateAnalyticsConfigRequest request);
}
