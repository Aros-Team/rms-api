package aros.services.rms.infraestructure.auth.jwt;

import org.springframework.stereotype.Service;

import aros.services.rms.core.auth.domain.AuthToken;
import aros.services.rms.core.auth.port.output.TokenPort;
import aros.services.rms.core.user.domain.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenAdapter implements TokenPort {
    private final JwtService jwtService;

    @Override
    public AuthToken generateToken(User user) {
        String access = jwtService.generateAccessToken(
            user.getEmail().value(),
            user.getRole(),
            user.getAssignedAreas()
        );
        String refresh = jwtService.generateRefreshToken(user.getEmail().value());
        return new AuthToken(access, refresh);
    }

    @Override
    public AuthToken generateTFAToken(User user) {
        String tfaToken = jwtService.generateTFAToken(user.getEmail().value());
        return new AuthToken(tfaToken, null);
    }
}
