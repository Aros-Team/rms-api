/* (C) 2026 */

package aros.services.rms.infraestructure.auth.api.dto;

import aros.services.rms.core.area.domain.Area;
import aros.services.rms.core.user.domain.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** Response DTO containing full user information including assigned areas. */
@Schema(description = "Información del usuario autenticado")
public record UserFullInfoResponse(
    @Schema(description = "ID del usuario", example = "1") Long id,
    @Schema(description = "Nombre completo", example = "John Doe") String name,
    @Schema(description = "Número de documento", example = "1234567890") String document,
    @Schema(description = "Email del usuario", example = "user@example.com") String email,
    @Schema(description = "Dirección", example = "123 Main Street") String address,
    @Schema(description = "Teléfono", example = "+1234567890") String phone,
    @Schema(description = "Rol del usuario", example = "ADMIN") UserRole role,
    @Schema(description = "Áreas asignadas al usuario") List<Area> areas) {}
