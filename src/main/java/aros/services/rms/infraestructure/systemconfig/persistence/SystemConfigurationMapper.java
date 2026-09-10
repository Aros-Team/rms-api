/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.persistence;

import aros.services.rms.core.systemconfig.domain.SystemConfiguration;
import aros.services.rms.core.systemconfig.domain.SystemConfigurationGroup;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/** Mapper for SystemConfiguration persistence operations. */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class SystemConfigurationMapper {

  /** Converts a SystemConfigurationEntity to a SystemConfiguration domain object. */
  public abstract SystemConfiguration toDomain(SystemConfigurationEntity entity);

  /** Converts a SystemConfiguration domain object to a SystemConfigurationEntity. */
  public abstract SystemConfigurationEntity toEntity(SystemConfiguration domain);

  /** Converts a SystemConfigurationGroupEntity to a SystemConfigurationGroup domain object. */
  public abstract SystemConfigurationGroup toDomainGroup(SystemConfigurationGroupEntity entity);

  /** Converts a SystemConfigurationGroup domain object to a SystemConfigurationGroupEntity. */
  public abstract SystemConfigurationGroupEntity toEntityGroup(SystemConfigurationGroup domain);
}
