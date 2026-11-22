/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.rest.mapper;

import aros.services.rms.core.analytics.domain.AlertsPage;
import aros.services.rms.core.analytics.domain.SystemAlert;
import aros.services.rms.core.analytics.infrastructure.rest.dto.AlertResponse;
import aros.services.rms.core.analytics.infrastructure.rest.dto.AlertsPageResponse;
import aros.services.rms.core.analytics.infrastructure.rest.dto.AlertsPageResponse.PageInfo;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** Maps a domain {@link AlertsPage} to its REST DTO. */
@Component
public class AlertsPageMapper {

  /**
   * Converts a domain page to its response DTO.
   *
   * @param page the domain page
   * @return the response DTO, or null
   */
  public AlertsPageResponse toResponse(AlertsPage page) {
    if (page == null) {
      return null;
    }
    List<AlertResponse> items =
        page.items() == null
            ? List.of()
            : page.items().stream().map(this::toItem).collect(Collectors.toList());
    return new AlertsPageResponse(
        items, new PageInfo(page.page().limit(), page.page().offset(), page.page().total()));
  }

  private AlertResponse toItem(SystemAlert alert) {
    return new AlertResponse(
        alert.getId(),
        alert.getSeverity() == null ? null : alert.getSeverity().name(),
        alert.getType() == null ? null : alert.getType().name(),
        alert.getMessage(),
        alert.getMetric(),
        alert.getBaseline(),
        alert.getCurrent(),
        alert.getDeviationPct(),
        alert.getPeriodKey(),
        alert.getPeriodBucket(),
        alert.getDetectedAt(),
        alert.getStatus() == null ? null : alert.getStatus().name());
  }
}
