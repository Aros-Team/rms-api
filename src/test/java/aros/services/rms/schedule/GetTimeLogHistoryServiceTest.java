/* (C) 2026 */

package aros.services.rms.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import aros.services.rms.core.schedule.application.service.GetTimeLogHistoryService;
import aros.services.rms.core.schedule.domain.LogType;
import aros.services.rms.core.schedule.domain.TimeLog;
import aros.services.rms.core.schedule.domain.TimeLogId;
import aros.services.rms.core.schedule.port.input.GetTimeLogHistoryUseCase.TimeLogFilter;
import aros.services.rms.core.schedule.port.output.TimeLogRepositoryPort;
import aros.services.rms.core.user.domain.UserId;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetTimeLogHistoryServiceTest {

  @Mock private TimeLogRepositoryPort timeLogRepository;

  private GetTimeLogHistoryService service;

  @BeforeEach
  void setUp() {
    service = new GetTimeLogHistoryService(timeLogRepository);
  }

  @Test
  void shouldReturnHistoryForWorker() {
    var workerId = UserId.of(1L);
    var filter = new TimeLogFilter(workerId, null, null, null);
    var log = new TimeLog(TimeLogId.of(1L), workerId, Instant.now(), LogType.IN, true, 1L);

    when(timeLogRepository.findByWorkerIdAndDateRange(eq(workerId), any(), any()))
        .thenReturn(List.of(log));

    var result = service.getHistory(filter);

    assertEquals(1, result.size());
  }

  @Test
  void shouldReturnAllHistory_whenNoWorkerFilter() {
    var filter = new TimeLogFilter(null, null, null, null);

    when(timeLogRepository.findAllByDateRange(any(), any())).thenReturn(List.of());

    var result = service.getHistory(filter);

    assertEquals(0, result.size());
  }
}
