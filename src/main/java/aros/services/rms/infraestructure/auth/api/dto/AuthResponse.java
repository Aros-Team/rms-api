package aros.services.rms.infraestructure.auth.api.dto;

public record AuthResponse(
    String type,
    String username,
    String accessToken,
    String refreshToken
) {}
