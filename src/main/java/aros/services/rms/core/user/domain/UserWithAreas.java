package aros.services.rms.core.user.domain;

import aros.services.rms.core.area.domain.Area;
import java.util.List;

public record UserWithAreas(
    UserId id,
    String document,
    String name,
    UserEmail email,
    String password,
    String address,
    String phone,
    UserRole role,
    List<Area> assignedAreas) {
  //
}
