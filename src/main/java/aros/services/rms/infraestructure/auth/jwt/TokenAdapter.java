/* (C) 2026 */
package aros.services.rms.infraestructure.auth.jwt;

import aros.services.rms.core.auth.port.output.TokenPort;
import aros.services.rms.core.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenAdapter implements TokenPort {
  private final JwtService jwtService;

  @Override
  public String generateAccessToken(User user) {
    return jwtService.generateAccessToken(
        user.getEmail().value(), user.getRole(), user.getAssignedAreas());
  }

  @Override
  public String generateTfaToken(User user) {
    return jwtService.generateTfaToken(user.getEmail().value());
  }

  @Override
  public String generateRefreshToken(User user) {
    return jwtService.generateRefreshToken(user.getEmail().value());
  }
}
