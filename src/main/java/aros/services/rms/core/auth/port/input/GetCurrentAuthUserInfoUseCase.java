package aros.services.rms.core.auth.port.input;

import aros.services.rms.core.auth.application.dto.UserFullInfo;
import aros.services.rms.core.auth.application.exception.UserNotFoundException;
import aros.services.rms.core.user.domain.UserEmail;

public interface GetCurrentAuthUserInfoUseCase {
    UserFullInfo getInfo(UserEmail email) throws UserNotFoundException;
}
