package aros.services.rms.infraestructure.auth.service;

import aros.services.rms.core.auth.application.dto.AuthUser;
import aros.services.rms.core.auth.port.output.CurrentUserPort;
import aros.services.rms.core.user.domain.UserRole;
import aros.services.rms.core.user.domain.UserWithAreas;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService implements CurrentUserPort {
  @Autowired private UserRepositoryPort userRepository;

  @Override
  public Optional<UserWithAreas> fetchCurrentUserInfo() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      return Optional.empty();
    }

    Jwt principal = (Jwt) authentication.getPrincipal();

    return userRepository.findByEmailWithAreas(principal.getSubject());
  }

  @Override
  public Optional<AuthUser> getAuthUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      return Optional.empty();
    }

    Jwt principal = (Jwt) authentication.getPrincipal();

    AuthUser authUser =
        new AuthUser(
            principal.getSubject(),
            UserRole.valueOf(principal.getClaimAsString("role")),
            principal.getClaim("areaIds"));

    return Optional.of(authUser);
  }
}
