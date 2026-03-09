package aros.services.rms.infraestructure.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

import aros.services.rms.core.area.domain.Area;
import aros.services.rms.core.user.domain.UserRole;

@Schema(description = "Response DTO con información completa del usuario autenticado")
public record UserFullInfoResponse (
    @Schema(description = "ID del usuario", example = "1") Long id,
    @Schema(description = "Número de documento", example = "1234567890") String document,
    @Schema(description = "Nombre completo", example = "Steven") String name,
    @Schema(description = "Email del usuario", example = "quintosteven590@gmail.com") String email,
    @Schema(description = "Contraseña encriptada (no exponer en producción)", example = "$2a$10$...") String password,
    @Schema(description = "Dirección", example = "Calle 123") String address,
    @Schema(description = "Teléfono", example = "+1234567890") String phone,
    @Schema(description = "Rol del usuario", example = "ADMIN") UserRole role,
    @Schema(description = "Áreas asignadas al usuario") List<Area> areas
) {
     
}
