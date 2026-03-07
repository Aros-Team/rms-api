package aros.services.rms.infraestructure.area.persistence.jpa;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import aros.services.rms.core.area.domain.Area;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AreaPersistenceMapper {

    @Mapping(source = "id", target = "id.value")
    Area toDomain(AreaEntity entity);

    @Mapping(source = "id.value", target = "id")
    AreaEntity toEntity(Area domain);
}
