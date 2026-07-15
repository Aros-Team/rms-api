package aros.services.rms.core.specialselection.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.specialselection.application.exception.SpecialSelectionNotFoundException;
import aros.services.rms.core.specialselection.domain.ChangeType;
import aros.services.rms.core.specialselection.domain.SpecialSelectionConfiguration;
import aros.services.rms.core.specialselection.domain.SpecialSelectionHistory;
import aros.services.rms.core.specialselection.domain.SpecialSelectionSnapshot;
import aros.services.rms.core.specialselection.port.output.SpecialSelectionHistoryRepositoryPort;
import aros.services.rms.core.specialselection.port.output.SpecialSelectionRepositoryPort;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link UpdateSpecialSelectionActiveService}. */
@ExtendWith(MockitoExtension.class)
class UpdateSpecialSelectionActiveServiceTest {

  private static final Long PRODUCT_ID = 10L;
  private static final String CHANGED_BY = "admin";

  @Mock private SpecialSelectionRepositoryPort repositoryPort;
  @Mock private SpecialSelectionHistoryRepositoryPort historyRepositoryPort;
  @Mock private SpecialSelectionSnapshotService snapshotService;

  private UpdateSpecialSelectionActiveService service;

  @BeforeEach
  void setUp() {
    service =
        new UpdateSpecialSelectionActiveService(
            repositoryPort, historyRepositoryPort, snapshotService);
  }

  // ---------------------------------------------------------------------------
  // UC-01: shouldSetActiveTrue_whenCurrentlyInactive
  // ---------------------------------------------------------------------------

  @Test
  void shouldSetActiveTrue_whenCurrentlyInactive() {
    SpecialSelectionConfiguration config =
        SpecialSelectionConfiguration.builder()
            .productId(PRODUCT_ID)
            .name("Combo")
            .active(false)
            .build();

    SpecialSelectionConfiguration saved =
        SpecialSelectionConfiguration.builder()
            .productId(PRODUCT_ID)
            .name("Combo")
            .active(true)
            .build();

    SpecialSelectionSnapshot snapshot = SpecialSelectionSnapshot.builder().active(true).build();

    when(repositoryPort.findById(PRODUCT_ID)).thenReturn(Optional.of(config));
    when(repositoryPort.save(any())).thenReturn(saved);
    when(historyRepositoryPort.findMaxVersionByProductId(PRODUCT_ID)).thenReturn(0);
    when(snapshotService.fromConfiguration(saved)).thenReturn(snapshot);
    when(snapshotService.toJson(snapshot)).thenReturn("{}");

    SpecialSelectionConfiguration result = service.execute(PRODUCT_ID, true, CHANGED_BY);

    assertTrue(result.isActive());
    verify(repositoryPort).save(config);
    verify(historyRepositoryPort).markAllAsNotCurrent(PRODUCT_ID);

    ArgumentCaptor<SpecialSelectionHistory> historyCaptor =
        ArgumentCaptor.forClass(SpecialSelectionHistory.class);
    verify(historyRepositoryPort).save(historyCaptor.capture());
    SpecialSelectionHistory recorded = historyCaptor.getValue();
    assertEquals(ChangeType.UPDATE, recorded.getChangeType());
    assertEquals(1, recorded.getVersion());
    assertTrue(recorded.isCurrent());
  }

  // ---------------------------------------------------------------------------
  // UC-02: shouldSetActiveFalse_whenCurrentlyActive
  // ---------------------------------------------------------------------------

  @Test
  void shouldSetActiveFalse_whenCurrentlyActive() {
    SpecialSelectionConfiguration config =
        SpecialSelectionConfiguration.builder()
            .productId(PRODUCT_ID)
            .name("Combo")
            .active(true)
            .build();

    SpecialSelectionConfiguration saved =
        SpecialSelectionConfiguration.builder()
            .productId(PRODUCT_ID)
            .name("Combo")
            .active(false)
            .build();

    SpecialSelectionSnapshot snapshot = SpecialSelectionSnapshot.builder().active(false).build();

    when(repositoryPort.findById(PRODUCT_ID)).thenReturn(Optional.of(config));
    when(repositoryPort.save(any())).thenReturn(saved);
    when(historyRepositoryPort.findMaxVersionByProductId(PRODUCT_ID)).thenReturn(2);
    when(snapshotService.fromConfiguration(saved)).thenReturn(snapshot);
    when(snapshotService.toJson(snapshot)).thenReturn("{}");

    SpecialSelectionConfiguration result = service.execute(PRODUCT_ID, false, CHANGED_BY);

    assertFalse(result.isActive());

    ArgumentCaptor<SpecialSelectionHistory> historyCaptor =
        ArgumentCaptor.forClass(SpecialSelectionHistory.class);
    verify(historyRepositoryPort).save(historyCaptor.capture());
    assertEquals(3, historyCaptor.getValue().getVersion());
  }

  // ---------------------------------------------------------------------------
  // UC-03: shouldThrowNotFound_whenProductIdDoesNotExist
  // ---------------------------------------------------------------------------

  @Test
  void shouldThrowNotFound_whenProductIdDoesNotExist() {
    when(repositoryPort.findById(PRODUCT_ID)).thenReturn(Optional.empty());

    assertThrows(
        SpecialSelectionNotFoundException.class,
        () -> service.execute(PRODUCT_ID, true, CHANGED_BY));

    verify(repositoryPort, never()).save(any());
    verify(historyRepositoryPort, never()).save(any());
  }
}
