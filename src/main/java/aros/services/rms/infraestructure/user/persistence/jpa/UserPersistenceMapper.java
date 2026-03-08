/* (C) 2026 */
package aros.services.rms.infraestructure.user.persistence.jpa;

import aros.services.rms.core.area.domain.AreaId;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.infraestructure.area.persistence.jpa.AreaEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserPersistenceMapper {

  @Mapping(source = "id", target = "id.value")
  @Mapping(source = "email", target = "email.value")
  @Mapping(source = "assignedAreas", target = "assignedAreas", qualifiedByName = "entityToAreaId")
  User toDomain(UserEntity entity);

  @Mapping(source = "id.value", target = "id")
  @Mapping(source = "email.value", target = "email")
  @Mapping(source = "assignedAreas", target = "assignedAreas", qualifiedByName = "areaIdToEntity")
  UserEntity toEntity(User domain);

  @Named("entityToAreaId")
  default List<AreaId> entityToAreaId(List<AreaEntity> entities) {
    if (entities == null) {
      return null;
    }
    return entities.stream().map(e -> AreaId.of(e.getId())).toList();
  }

  @Named("areaIdToEntity")
  default List<AreaEntity> areaIdToEntity(List<AreaId> areaIds) {
    if (areaIds == null) {
      return null;
    }
    return areaIds.stream()
        .map(
            id -> {
              AreaEntity entity = new AreaEntity();
              entity.setId(id.value());
              return entity;
            })
        .toList();
  }
}
