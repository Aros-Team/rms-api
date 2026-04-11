package aros.services.rms.core.auth.application.dto;

import aros.services.rms.core.user.domain.UserRole;
import java.util.List;

public record AuthUser(String username, UserRole role, List<Long> areaIds) {
  //
}
