/* (C) 2026 */
package aros.services.rms.infraestructure.auth.jwt;

import aros.services.rms.core.area.domain.AreaId;
import aros.services.rms.core.user.domain.UserRole;
import java.util.List;

public interface JwtService {
  String generateAccessToken(String username, UserRole role, List<AreaId> areas);

  String generateRefreshToken(String username);

  String generateTFAToken(String username);

  boolean validateToken(String token);

  String getUsernameFromToken(String token);

  UserRole getRoleFromToken(String token);

  List<AreaId> getAreasFromToken(String token);
}
