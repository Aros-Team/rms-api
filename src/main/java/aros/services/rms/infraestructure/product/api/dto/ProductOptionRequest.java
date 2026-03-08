/* (C) 2026 */
package aros.services.rms.infraestructure.product.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for creating or updating a product option.
 *
 * @param name the option name
 * @param optionCategoryId the option category id this option belongs to
 */
public record ProductOptionRequest(
    @NotBlank(message = "Option name is required") String name,
    @NotNull(message = "Option category ID is required") Long optionCategoryId) {}