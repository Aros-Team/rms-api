package aros.services.rms.infraestructure.auth.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "El username es requerido")
    @Email(message = "Formato de email inválido")
    String username,

    @NotBlank(message = "La contraseña es requerida")
    String password,

    String deviceHash
) {}
