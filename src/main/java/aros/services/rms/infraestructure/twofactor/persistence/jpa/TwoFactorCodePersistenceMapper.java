/* (C) 2026 */
package aros.services.rms.infraestructure.twofactor.persistence.jpa;

import aros.services.rms.core.twofactor.domain.TwoFactorCode;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.infraestructure.user.persistence.jpa.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TwoFactorCodePersistenceMapper {

  default UserId userToUserId(UserEntity user) {
    return user != null ? UserId.of(user.getId()) : null;
  }

  default UserEntity userIdToUser(UserId userId) {
    if (userId == null) return null;
    UserEntity user = new UserEntity();
    user.setId(userId.value());
    return user;
  }

  @Mapping(source = "id", target = "id.id")
  @Mapping(source = "user", target = "userId")
  TwoFactorCode toDomain(TwoFactorCodeEntity entity);

  @Mapping(source = "id.id", target = "id")
  @Mapping(source = "userId", target = "user")
  TwoFactorCodeEntity toEntity(TwoFactorCode domain);
}
