package aros.services.rms.core.user.port.dto;

import aros.services.rms.core.user.domain.UserEmail;

public record CreateUserInfo (
    String document,
    String name,
    UserEmail email,
    String address,
    String phone
) {

}
