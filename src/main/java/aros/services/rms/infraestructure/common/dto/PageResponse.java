/* (C) 2026 */

package aros.services.rms.infraestructure.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Generic paginated response wrapper using the Spring Data {@code {content, page}} dialect.
 *
 * <p>This is the single pagination shape for offset/page-number endpoints. Bare list endpoints
 * return a plain array; the alerts endpoint keeps its documented {@code {items, page:{limit,offset,
 * total}}} offset shape. See {@code docs/contracts/pagination.md}.
 */
@Schema(description = "Paginated response in the Spring {content, page} dialect")
public record PageResponse<T>(
    @Schema(description = "Page items") List<T> content,
    @Schema(description = "Page metadata") PageMeta page) {

  /** Spring-compatible page metadata block. */
  @Schema(description = "Page metadata")
  public record PageMeta(
      @Schema(description = "Current page number (0-based)", example = "0") int number,
      @Schema(description = "Page size", example = "20") int size,
      @Schema(description = "Total number of items", example = "100") long totalElements,
      @Schema(description = "Total number of pages", example = "5") int totalPages) {}

  /**
   * Creates a paginated response from items and pagination information.
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
    return new PageResponse<>(items, new PageMeta(page, size, total, totalPages));
  }
}
