/* (C) 2026 */
package aros.services.rms.infraestructure.auth.persistence.jpa;

import aros.services.rms.core.auth.domain.AccountSetupToken;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.infraestructure.user.persistence.jpa.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/** Mapper for account setup tokens. */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AccountSetupTokenMapper {

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
   * Converts an entity to domain.
   *
   * @param entity the entity
   * @return the domain
   */
  @Mapping(source = "user", target = "userId")
  AccountSetupToken toDomain(AccountSetupTokenEntity entity);

  /**
   * Converts a domain to entity.
   *
   * @param domain the domain
   * @return the entity
   */
  @Mapping(source = "userId", target = "user")
  AccountSetupTokenEntity toEntity(AccountSetupToken domain);
}
