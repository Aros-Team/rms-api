package aros.services.rms.infraestructure.auth.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyTwoFactorRequest(
    @NotBlank(message = "El username es requerido")
    @Email(message = "Formato de email inválido")
    String username,

    @NotBlank(message = "El código es requerido")
    @Size(min = 6, max = 6, message = "El código debe tener 6 dígitos")
    String code,

    String deviceHash
) {}
