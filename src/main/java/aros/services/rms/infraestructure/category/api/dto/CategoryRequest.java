/* (C) 2026 */
package aros.services.rms.infraestructure.category.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for creating or updating a product category.
 *
 * @param name the category name
 * @param description the category description
 */
public record CategoryRequest(
    @NotBlank(message = "Category name is required") String name, String description) {}