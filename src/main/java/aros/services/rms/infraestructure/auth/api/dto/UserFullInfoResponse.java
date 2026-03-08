package aros.services.rms.infraestructure.auth.api.dto;

import java.util.List;

import aros.services.rms.core.area.domain.Area;
import aros.services.rms.core.user.domain.UserRole;

public record UserFullInfoResponse (
    Long id,
    String document,
    String name,
    String email,
    String password,
    String address,
    String phone,
    UserRole role,
    List<Area> areas
) {
    
}
