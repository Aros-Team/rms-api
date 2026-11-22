/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** Response DTO for a page of variance alerts. */
@Schema(description = "Paged alerts")
public record AlertsPageResponse(List<AlertResponse> items, PageInfo page) {

  /** Paging metadata. */
  @Schema(description = "Paging metadata")
  public record PageInfo(int limit, int offset, long total) {}
}
