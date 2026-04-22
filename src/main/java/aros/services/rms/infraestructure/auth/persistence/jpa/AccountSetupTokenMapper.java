/* (C) 2026 */
package aros.services.rms.infraestructure.auth.persistence.jpa;

import aros.services.rms.core.auth.domain.AccountSetupToken;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.infraestructure.user.persistence.jpa.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AccountSetupTokenMapper {

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

  @Mapping(source = "user", target = "userId")
  AccountSetupToken toDomain(AccountSetupTokenEntity entity);

  @Mapping(source = "userId", target = "user")
  AccountSetupTokenEntity toEntity(AccountSetupToken domain);
}
