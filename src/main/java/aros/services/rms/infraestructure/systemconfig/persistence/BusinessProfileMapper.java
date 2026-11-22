/* (C) 2026 */

package aros.services.rms.infraestructure.systemconfig.persistence;

import aros.services.rms.core.systemconfig.domain.BusinessProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/** Mapper for the business profile singleton. */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BusinessProfileMapper {

  /**
   * Converts the entity to the domain profile.
   *
   * @param entity the persisted entity
   * @return the domain profile
   */
  BusinessProfile toDomain(BusinessProfileEntity entity);

  /**
   * Converts the domain profile to a singleton entity with id 1.
   *
   * @param domain the domain profile
   * @return the entity
   */
  @Mapping(target = "id", constant = "1L")
  @Mapping(target = "updatedAt", ignore = true)
  BusinessProfileEntity toEntity(BusinessProfile domain);
}
