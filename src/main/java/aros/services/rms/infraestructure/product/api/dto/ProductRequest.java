/* (C) 2026 */
package aros.services.rms.infraestructure.product.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request DTO for creating or updating a product.
 *
 * @param name the product name
 * @param basePrice the product base price
 * @param hasOptions whether the product supports customization options
 * @param categoryId the category id the product belongs to
 * @param areaId the preparation area id
 */
public record ProductRequest(
    @NotBlank(message = "Product name is required") String name,
    @NotNull(message = "Base price is required") @Positive(message = "Base price must be positive")
        Double basePrice,
    @NotNull(message = "hasOptions flag is required") Boolean hasOptions,
    @NotNull(message = "Category ID is required") Long categoryId,
    @NotNull(message = "Area ID is required") Long areaId) {}