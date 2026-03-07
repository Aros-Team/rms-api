package aros.services.rms.infraestructure.auth.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import aros.services.rms.core.auth.application.dto.AuthResult;
import aros.services.rms.core.auth.application.dto.Credentials;
import aros.services.rms.core.auth.application.dto.TwoFactorCredentials;
import aros.services.rms.core.auth.application.exception.InvalidCredentialsException;
import aros.services.rms.core.auth.port.input.LoginUseCase;
import aros.services.rms.core.auth.port.input.VerifyTwoFactorUseCase;
import aros.services.rms.core.user.domain.UserEmail;
import aros.services.rms.infraestructure.auth.api.dto.AuthResponse;
import aros.services.rms.infraestructure.auth.api.dto.LoginRequest;
import aros.services.rms.infraestructure.auth.api.dto.VerifyTwoFactorRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final LoginUseCase loginUseCase;
    private final VerifyTwoFactorUseCase verifyTwoFactorUseCase;
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) throws InvalidCredentialsException {
        Credentials credentials = new Credentials(
            new UserEmail(request.username()),
            request.password(),
            request.deviceHash()
        );
        
        AuthResult result = loginUseCase.authenticate(credentials);
        
        AuthResponse response = new AuthResponse(
            result.type().name(),
            result.username(),
            result.token().getAccess(),
            result.token().getRefresh()
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/verify-tfa")
    public ResponseEntity<AuthResponse> verifyTfa(@Valid @RequestBody VerifyTwoFactorRequest request) throws InvalidCredentialsException {
        TwoFactorCredentials credentials = new TwoFactorCredentials(
            new UserEmail(request.username()),
            request.code(),
            request.deviceHash()
        );
        
        AuthResult result = verifyTwoFactorUseCase.verify(credentials);
        
        AuthResponse response = new AuthResponse(
            result.type().name(),
            result.username(),
            result.token().getAccess(),
            result.token().getRefresh()
        );
        
        return ResponseEntity.ok(response);
    }
}
