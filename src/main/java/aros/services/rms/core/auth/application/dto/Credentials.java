package aros.services.rms.core.auth.application.dto;

import aros.services.rms.core.user.domain.UserEmail;

public record Credentials (
    UserEmail username,
    String password,
    String deviceHash
) {
}
