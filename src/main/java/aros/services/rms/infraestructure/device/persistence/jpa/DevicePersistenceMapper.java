package aros.services.rms.infraestructure.device.persistence.jpa;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import aros.services.rms.core.device.domain.Device;
import aros.services.rms.core.user.domain.UserId;

import aros.services.rms.infraestructure.user.persistence.jpa.UserEntity;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DevicePersistenceMapper {

    default UserId userToUserId(UserEntity user) {
        if (user == null) {
            return null;
        }
        return UserId.of(user.getId());
    }

    default UserEntity userIdToUser(UserId userId) {
        if (userId == null) {
            return null;
        }
        UserEntity user = new UserEntity();
        user.setId(userId.value());
        return user;
    }

    @Mapping(source = "id", target = "id.id")
    @Mapping(source = "user", target = "userId")
    Device toDomain(DeviceEntity entity);

    @Mapping(source = "id.id", target = "id")
    @Mapping(source = "userId", target = "user")
    DeviceEntity toEntity(Device domain);
}
