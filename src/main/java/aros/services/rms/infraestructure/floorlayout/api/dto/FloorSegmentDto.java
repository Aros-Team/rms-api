/* (C) 2026 */

package aros.services.rms.infraestructure.floorlayout.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * One inclusive row run in the floor grid.
 *
 * @param row zero-based row, top to bottom
 * @param from inclusive first column
 * @param to inclusive last column
 */
@Schema(description = "Inclusive row run in the floor grid")
public record FloorSegmentDto(
    @Schema(description = "Zero-based row", example = "0") int row,
    @Schema(description = "Inclusive first column", example = "2") int from,
    @Schema(description = "Inclusive last column", example = "6") int to) {}
