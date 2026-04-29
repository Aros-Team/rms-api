/* (C) 2026 */

package aros.services.rms.core.user.domain;

import aros.services.rms.core.area.domain.Area;
import java.util.List;

/** Record representing a user with their assigned areas for querying purposes. */
public record UserWithAreas(
    UserId id,
    String document,
    String name,
    UserEmail email,
    String password,
    String address,
    String phone,
    UserRole role,
    UserStatus status,
    List<Area> assignedAreas) {
  //
}
