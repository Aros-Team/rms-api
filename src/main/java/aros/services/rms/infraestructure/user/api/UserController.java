package aros.services.rms.infraestructure.user.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import aros.services.rms.core.user.application.exception.UserAlreadyExistsException;
import aros.services.rms.core.user.port.input.CreateUserUseCase;
import aros.services.rms.infraestructure.share.security.JustAdminUser;
import aros.services.rms.infraestructure.user.api.dto.UserRegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/users")
public class UserController {
    private final CreateUserUseCase createUserUseCase;

    @PostMapping
    @JustAdminUser
    public ResponseEntity<Void> register(@Valid @RequestBody UserRegisterRequest request) throws UserAlreadyExistsException {
        this.createUserUseCase.create(request.toCreateUserInfo());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
