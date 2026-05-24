/* (C) 2026 */

package aros.services.rms.schedule;

import static org.mockito.Mockito.verify;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.schedule.application.service.RemoveScheduleFromWorkerService;
import aros.services.rms.core.schedule.domain.WorkerScheduleAssignmentId;
import aros.services.rms.core.schedule.port.output.WorkerScheduleAssignmentRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    var assignmentId = WorkerScheduleAssignmentId.of(1L);

    service.remove(assignmentId);

    verify(assignmentRepository).delete(assignmentId);
  }
}
