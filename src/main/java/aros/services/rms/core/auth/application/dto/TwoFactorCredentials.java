package aros.services.rms.core.auth.application.dto;

import aros.services.rms.core.user.domain.UserEmail;

public record TwoFactorCredentials(
    UserEmail username,
    String code,
    String deviceHash
) {}
