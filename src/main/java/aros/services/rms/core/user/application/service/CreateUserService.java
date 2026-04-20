package aros.services.rms.core.user.application.service;

import aros.services.rms.core.auth.port.output.PasswordEncoderPort;
import aros.services.rms.core.email.application.service.EmailService;
import aros.services.rms.core.email.port.input.RegistrationEmailUseCase;
import aros.services.rms.core.user.application.exception.UserAlreadyExistsException;
import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserRole;
import aros.services.rms.core.user.domain.UserStatus;
import aros.services.rms.core.user.port.dto.CreateUserInfo;
import aros.services.rms.core.user.port.input.CreateUserUseCase;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import java.util.List;

public class CreateUserService implements CreateUserUseCase {
  private final UserRepositoryPort userPort;
  private final RegistrationEmailUseCase registrationEmailUseCase;
  private final PasswordEncoderPort passwordPort;

  public CreateUserService(
      UserRepositoryPort userPort,
      RegistrationEmailUseCase registrationEmailUseCase,
      PasswordEncoderPort passwordPort) {
    this.userPort = userPort;
    this.registrationEmailUseCase = registrationEmailUseCase;
    this.passwordPort = passwordPort;
  }

  @Override
  public CreateUserResult create(CreateUserInfo info) throws UserAlreadyExistsException {
    boolean exists = this.userPort.existsByEmailOrDocument(info.document(), info.email().value());

    if (exists) {
      throw new UserAlreadyExistsException("User document or email already used");
    }

    String password = GenerateSecurePasswordService.execute();

    User user =
        new User(
            null,
            info.document(),
            info.name(),
            info.email(),
            this.passwordPort.encode(password),
            info.address(),
            info.phone(),
            UserRole.WORKER,
            UserStatus.PENDING,
            List.of());

    User saved = this.userPort.save(user);

    boolean emailSent = true;
    try {
      if (this.registrationEmailUseCase instanceof EmailService emailService) {
        emailSent =
            emailService.sendRegistrationMailSync(
                saved.getEmail(),
                String.format(
                    "Haz sido registrado en nuestro sistema, para ingresar haz uso de la siguiente contraseña: %s",
                    password));
      } else {
        this.registrationEmailUseCase.sendRegistrationMail(
            saved.getEmail(),
            String.format(
                "Haz sido registrado en nuestro sistema, para ingresar haz uso de la siguiente contraseña: %s",
                password));
      }
    } catch (Exception e) {
      emailSent = false;
    }

    if (!emailSent) {
      saved.markAsError();
      saved = this.userPort.save(saved);
    }

    return new CreateUserResult(saved, password);
  }
}
