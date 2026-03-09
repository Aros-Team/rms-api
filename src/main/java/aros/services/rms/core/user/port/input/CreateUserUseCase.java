package aros.services.rms.core.user.port.input;

import aros.services.rms.core.user.application.exception.UserAlreadyExistsException;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.port.dto.CreateUserInfo;

public interface CreateUserUseCase {
    public User create(CreateUserInfo info) throws UserAlreadyExistsException;
}
