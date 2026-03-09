package aros.services.rms.core.email.port.input;

import aros.services.rms.core.user.domain.UserEmail;

public interface RegistrationEmailUseCase {
    public void sendRegistrationMail(UserEmail destination, String message);
} 