/* (C) 2026 */
package aros.services.rms.infraestructure.user.persistence.jpa;

import aros.services.rms.core.area.domain.AreaId;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserWithAreas;
import aros.services.rms.infraestructure.area.persistence.jpa.Area;
import aros.services.rms.infraestructure.area.persistence.jpa.AreaMapper;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    uses = {AreaMapper.class})
public abstract class UserPersistenceMapper {

  @Autowired protected AreaMapper areaMapper;

  @Mapping(source = "id", target = "id.value")
  @Mapping(source = "email", target = "email.value")
  @Mapping(source = "assignedAreas", target = "assignedAreas", qualifiedByName = "entityToAreaId")
  public abstract User toDomain(UserEntity entity);

  @Mapping(source = "id", target = "id.value")
  @Mapping(source = "email", target = "email.value")
  @Mapping(source = "assignedAreas", target = "assignedAreas", qualifiedByName = "entityToArea")
  public abstract UserWithAreas toUserWithAreas(UserEntity entity);

  @Mapping(source = "id.value", target = "id")
  @Mapping(source = "email.value", target = "email")
  @Mapping(source = "assignedAreas", target = "assignedAreas", qualifiedByName = "areaIdToEntity")
  public abstract UserEntity toEntity(User domain);

  @Named("entityToArea")
  List<aros.services.rms.core.area.domain.Area> entityToArea(List<Area> entities) {
    if (entities == null) {
      return null;
    }
    return entities.stream().map(e -> this.areaMapper.toDomain(e)).toList();
  }

  @Named("entityToAreaId")
  List<AreaId> entityToAreaId(List<Area> entities) {
    if (entities == null) {
      return null;
    }
    return entities.stream().map(e -> AreaId.of(e.getId())).toList();
  }

  @Named("areaIdToEntity")
  List<Area> areaIdToEntity(List<AreaId> areaIds) {
    if (areaIds == null) {
      return null;
    }
    return areaIds.stream()
        .map(
            id -> {
              Area entity = new Area();
              entity.setId(id.value());
              return entity;
            })
        .toList();
  }
}
