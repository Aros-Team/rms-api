/* (C) 2026 */

package aros.services.rms.infraestructure.device.persistence.jpa;

import aros.services.rms.core.device.domain.Device;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.infraestructure.user.persistence.jpa.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/** Mapper for device persistence. */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DevicePersistenceMapper {

  /**
   * Converts a user entity to a user ID.
   *
   * @param user the user entity
   * @return the user ID
   */
  default UserId userToUserId(UserEntity user) {
    if (user == null) {
      return null;
    }
    return UserId.of(user.getId());
  }

  /**
   * Converts a user ID to a user entity.
   *
   * @param userId the user ID
   * @return the user entity
   */
  default UserEntity userIdToUser(UserId userId) {
    if (userId == null) {
      return null;
    }
    UserEntity user = new UserEntity();
    user.setId(userId.value());
    return user;
  }

  /**
   * Converts a device entity to a device domain object.
   *
   * @param entity the device entity
   * @return the device domain object
   */
  @Mapping(source = "id", target = "id.id")
  @Mapping(source = "user", target = "userId")
  Device toDomain(DeviceEntity entity);

  /**
   * Converts a device domain object to a device entity.
   *
   * @param domain the device domain object
   * @return the device entity
   */
  @Mapping(source = "id.id", target = "id")
  @Mapping(source = "userId", target = "user")
  DeviceEntity toEntity(Device domain);
}
