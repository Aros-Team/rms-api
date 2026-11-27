/* (C) 2026 */

package aros.services.rms.infraestructure.auth.jwt;

import aros.services.rms.core.area.domain.AreaActivity;
import aros.services.rms.core.area.domain.AreaId;
import aros.services.rms.core.area.port.output.WorkerAreaActivityPort;
import aros.services.rms.core.auth.port.output.TokenPort;
import aros.services.rms.core.user.domain.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Adapter for token operations. */
@Service
@RequiredArgsConstructor
public class TokenAdapter implements TokenPort {
  private final JwtService jwtService;
  private final WorkerAreaActivityPort workerAreaActivityPort;

  @Override
  public String generateAccessToken(User user, boolean restricted) {
    return jwtService.generateAccessToken(
        user.getEmail().value(),
        user.getRole(),
        user.getAssignedAreas(),
        resolveAreaTypes(
            user.getAssignedAreas(), user.getId() != null ? user.getId().value() : null),
        restricted);
  }

  @Override
  public String generateTfaToken(User user) {
    return jwtService.generateTfaToken(user.getEmail().value());
  }

  @Override
  public String generateRefreshToken(User user) {
    return jwtService.generateRefreshToken(user.getEmail().value());
  }

  /**
   * Resolves the area type names assigned to the user so authorization rules can match on the
   * semantic area (SERVICE, KITCHEN, ...) instead of on database identifiers.
   *
   * @param areas the user's assigned area ids
   * @return the matching area type names, empty when the user has no areas
   */
  private List<String> resolveAreaTypes(List<AreaId> areas, Long userId) {
    if (areas == null || areas.isEmpty()) {
      return List.of();
    }
    List<Long> ids = areas.stream().map(AreaId::value).toList();
    java.util.Set<AreaActivity> activities = new java.util.LinkedHashSet<>();
    if (userId != null) {
      workerAreaActivityPort.findByUserId(userId).values().forEach(activities::addAll);
    }
    if (activities.isEmpty()) {
      activities.addAll(
          workerAreaActivityPort.findAreaActivities(ids).values().stream()
              .flatMap(java.util.Collection::stream)
              .toList());
    }
    return activities.stream().map(Enum::name).sorted().toList();
  }
}
