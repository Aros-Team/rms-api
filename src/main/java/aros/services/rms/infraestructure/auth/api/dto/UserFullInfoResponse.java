/* (C) 2026 */

package aros.services.rms.infraestructure.auth.api.dto;

import aros.services.rms.core.area.domain.Area;
import aros.services.rms.core.user.domain.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** Response DTO containing full user information including assigned areas. */
@Schema(description = "Authenticated user information")
public record UserFullInfoResponse(
    @Schema(description = "User ID", example = "1") Long id,
    @Schema(description = "Full name", example = "John Doe") String name,
    @Schema(description = "Document number", example = "1234567890") String document,
    @Schema(description = "User email", example = "user@example.com") String email,
    @Schema(description = "Address", example = "123 Main Street") String address,
    @Schema(description = "Phone number", example = "+1234567890") String phone,
    @Schema(description = "User role", example = "ADMIN") UserRole role,
    @Schema(description = "Areas assigned to the user") List<Area> areas) {}
