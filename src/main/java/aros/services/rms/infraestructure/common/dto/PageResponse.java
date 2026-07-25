/* (C) 2026 */

package aros.services.rms.infraestructure.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** Generic paginated response wrapper. */
@Schema(description = "Generic paginated response")
public record PageResponse<T>(
    @Schema(description = "Page items") List<T> items,
    @Schema(description = "Total number of items", example = "100") long total,
    @Schema(description = "Current page number (0-based)", example = "0") int page,
    @Schema(description = "Page size", example = "20") int size,
    @JsonProperty("total_pages") @Schema(description = "Total number of pages", example = "5")
        int totalPages) {

  /**
   * Creates a paginated response from items and pagination info.
   *
   * @param items the page items
   * @param total the total number of items
   * @param page the current page (0-based)
   * @param size the page size
   * @param <T> the item type
   * @return a new PageResponse
   */
  public static <T> PageResponse<T> of(List<T> items, long total, int page, int size) {
    int totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 0;
    return new PageResponse<>(items, total, page, size, totalPages);
  }
}
