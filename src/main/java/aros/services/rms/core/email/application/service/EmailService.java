package aros.services.rms.core.email.application.service;

import java.util.Map;

import aros.services.rms.core.email.domain.Email;
import aros.services.rms.core.email.port.input.TwoFactorAuthEmailUseCase;
import aros.services.rms.core.email.port.output.EmailServicePort;
import aros.services.rms.core.user.domain.UserEmail;

public class EmailService implements TwoFactorAuthEmailUseCase {
    private final EmailServicePort emailPort;

    public EmailService(EmailServicePort emailPort) {
        this.emailPort = emailPort;
    }

    @Override
    public void sendTwoFactorCode(UserEmail email, String code) {
        Email tfaEmail = new Email(
            email.value(),
            "admin@aros.service",
            "notification",
            Map.of("code", code)
        );

        this.emailPort.send(tfaEmail);
    }
}
