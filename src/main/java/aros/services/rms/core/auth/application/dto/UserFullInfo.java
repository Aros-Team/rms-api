package aros.services.rms.core.auth.application.dto;

import aros.services.rms.core.area.domain.Area;
import aros.services.rms.core.user.domain.UserEmail;
import aros.services.rms.core.user.domain.UserId;
import aros.services.rms.core.user.domain.UserRole;
import java.util.List;

public record UserFullInfo(
    UserId id,
    String document,
    String name,
    UserEmail email,
    String password,
    String address,
    String phone,
    UserRole role,
    List<Area> areas) {}
