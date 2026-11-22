/* (C) 2026 */

package aros.services.rms.core.analytics.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import aros.services.rms.core.analytics.domain.AlertStatus;
import aros.services.rms.core.analytics.domain.AlertsPage;
import aros.services.rms.core.analytics.domain.SystemAlert;
import aros.services.rms.core.analytics.domain.port.out.SystemAlertRepositoryPort;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link GetAlertsService}.
 *
 * <p><b>Feature:</b> Paged variance alert reads with optional filters.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>{@code limit} is clamped to 100 and a non-positive value defaults to 50
 *   <li>A negative {@code offset} becomes zero
 *   <li>Filters are forwarded unchanged to the repository
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Mocks {@link SystemAlertRepositoryPort} and verifies the arguments it receives
 *   <li>Executes the service with an oversized limit and with a missing limit
 *   <li>Asserts the effective paging metadata
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class GetAlertsServiceTest {

  @Mock private SystemAlertRepositoryPort alertRepositoryPort;

  @Test
  void should_clamp_limit_to_maximum_and_keep_offset() {
    when(alertRepositoryPort.find(AlertStatus.OPEN, null, "monthly", "2026-01", "2026-12", 100, 20))
        .thenReturn(List.of(new SystemAlert()));
    when(alertRepositoryPort.count(AlertStatus.OPEN, null, "monthly", "2026-01", "2026-12"))
        .thenReturn(134L);

    AlertsPage page =
        new GetAlertsService(alertRepositoryPort)
            .execute(AlertStatus.OPEN, null, "monthly", "2026-01", "2026-12", 500, 20);

    assertEquals(100, page.page().limit());
    assertEquals(20, page.page().offset());
    assertEquals(134, page.page().total());
    assertEquals(1, page.items().size());
  }

  @Test
  void should_default_limit_when_missing() {
    when(alertRepositoryPort.find(null, null, null, null, null, 50, 0)).thenReturn(List.of());
    when(alertRepositoryPort.count(null, null, null, null, null)).thenReturn(0L);

    AlertsPage page =
        new GetAlertsService(alertRepositoryPort).execute(null, null, null, null, null, 0, -5);

    assertEquals(50, page.page().limit());
    assertEquals(0, page.page().offset());
  }
}
