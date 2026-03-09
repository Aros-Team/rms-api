package aros.services.rms.infraestructure.user.api.dto;

import aros.services.rms.core.user.domain.UserEmail;
import aros.services.rms.core.user.port.dto.CreateUserInfo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserRegisterRequest (
    @NotBlank
    @Pattern(message = "the document must only have numbers", regexp = "\\d+")
    String document,
    @NotBlank
    String name,
    @NotBlank
    @Email
    String email,
    String address,
    @Pattern(message = "the phone must be a valid phone number", regexp = "\\d{7,10}")
    String phone
) {
    public CreateUserInfo toCreateUserInfo() {
        return new CreateUserInfo(
            document, 
            name,
            new UserEmail(email),
            address,
            phone
        );
    }
}
