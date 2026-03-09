/* (C) 2026 */
package aros.services.rms.infraestructure.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication response DTO")
public record AuthResponse(
    @Schema(description = "Authentication result type", example = "SUCCESS")
        String type,
    @Schema(description = "User email/username", example = "user@example.com")
        String username,
    @Schema(description = "JWT access token", example = "eyJhbGciOiJSUzI1Ni...")
        String accessToken,
    @Schema(description = "JWT refresh token", example = "eyJhbGciOiJSUzI1Ni...") 
        String refreshToken) {}
