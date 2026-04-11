package aros.services.rms.core.auth.port.output;

import aros.services.rms.core.auth.application.dto.AuthUser;
import aros.services.rms.core.user.domain.UserWithAreas;
import java.util.Optional;

public interface CurrentUserPort {
  Optional<UserWithAreas> fetchCurrentUserInfo();

  Optional<AuthUser> getAuthUser();
}
