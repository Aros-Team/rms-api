/* (C) 2026 */
package aros.services.rms.infraestructure.user.api.dto;

import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.domain.UserRole;
import aros.services.rms.core.user.domain.UserStatus;
import java.util.List;

public record UserResponse(
    Long id,
    String document,
    String name,
    String email,
    String address,
    String phone,
    UserRole role,
    UserStatus status,
    List<Long> assignedAreas) {

  public static UserResponse fromDomain(User user) {
    return new UserResponse(
        user.getId().value(),
        user.getDocument(),
        user.getName(),
        user.getEmail().value(),
        user.getAddress(),
        user.getPhone(),
        user.getRole(),
        user.getStatus(),
        user.getAssignedAreas().stream().map(areaId -> areaId.value()).toList());
  }
}
