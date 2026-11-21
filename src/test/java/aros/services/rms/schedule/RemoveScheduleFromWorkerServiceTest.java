/* (C) 2026 */

package aros.services.rms.schedule;

import static org.mockito.Mockito.verify;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.schedule.application.service.RemoveScheduleFromWorkerService;
import aros.services.rms.core.schedule.domain.ScheduleId;
import aros.services.rms.core.schedule.port.output.WorkerScheduleAssignmentRepositoryPort;
import aros.services.rms.core.user.domain.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link RemoveScheduleFromWorkerService}.
 *
 * <p><b>Feature:</b> Schedule removal from workers
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>set Up
 *   <li>should Remove Assignment Successfully
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Mocks Logger, WorkerScheduleAssignmentRepositoryPort dependencies via Mockito
 *   <li>Verifies business logic with unit-level assertions
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class RemoveScheduleFromWorkerServiceTest {

  @Mock private WorkerScheduleAssignmentRepositoryPort assignmentRepository;
  @Mock private Logger logger;

  private RemoveScheduleFromWorkerService service;

  @BeforeEach
  void setUp() {
    service = new RemoveScheduleFromWorkerService(assignmentRepository, logger);
  }

  @Test
  void shouldRemoveAssignmentSuccessfully() {
    var workerId = UserId.of(1L);
    var scheduleId = ScheduleId.of(2L);

    service.remove(workerId, scheduleId);

    verify(assignmentRepository).deleteByWorkerIdAndScheduleId(workerId, scheduleId);
  }
}
