/* (C) 2026 */
package aros.services.rms.infraestructure.twofactor.persistence.jpa;

import aros.services.rms.core.twofactor.domain.TwoFactorCode;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.infraestructure.user.persistence.jpa.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/** Mapper for two-factor code persistence. */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TwoFactorCodePersistenceMapper {

  /**
   * Converts a user entity to a user ID.
   *
   * @param user the user entity
   * @return the user ID
   */
  default UserId userToUserId(UserEntity user) {
    return user != null ? UserId.of(user.getId()) : null;
  }

  /**
   * Converts a user ID to a user entity.
   *
   * @param userId the user ID
   * @return the user entity
   */
  default UserEntity userIdToUser(UserId userId) {
    if (userId == null) return null;
    UserEntity user = new UserEntity();
    user.setId(userId.value());
    return user;
  }

  /**
   * Converts a two-factor code entity to a domain object.
   *
   * @param entity the entity
   * @return the domain object
   */
  @Mapping(source = "id", target = "id.id")
  @Mapping(source = "user", target = "userId")
  TwoFactorCode toDomain(TwoFactorCodeEntity entity);

  /**
   * Converts a two-factor code domain object to an entity.
   *
   * @param domain the domain object
   * @return the entity
   */
  @Mapping(source = "id.id", target = "id")
  @Mapping(source = "userId", target = "user")
  TwoFactorCodeEntity toEntity(TwoFactorCode domain);
}
