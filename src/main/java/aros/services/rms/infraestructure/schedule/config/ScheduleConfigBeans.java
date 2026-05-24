package aros.services.rms.infraestructure.schedule.config;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.schedule.application.service.AssignScheduleToWorkerService;
import aros.services.rms.core.schedule.application.service.CreateScheduleService;
import aros.services.rms.core.schedule.application.service.DeleteScheduleService;
import aros.services.rms.core.schedule.application.service.GetTimeLogHistoryService;
import aros.services.rms.core.schedule.application.service.GetWorkerShiftsService;
import aros.services.rms.core.schedule.application.service.RecordTimeLogService;
import aros.services.rms.core.schedule.application.service.RemoveScheduleFromWorkerService;
import aros.services.rms.core.schedule.application.service.UpdateScheduleService;
import aros.services.rms.core.schedule.port.input.AssignScheduleToWorkerUseCase;
import aros.services.rms.core.schedule.port.input.CreateScheduleUseCase;
import aros.services.rms.core.schedule.port.input.DeleteScheduleUseCase;
import aros.services.rms.core.schedule.port.input.GetTimeLogHistoryUseCase;
import aros.services.rms.core.schedule.port.input.GetWorkerShiftsUseCase;
import aros.services.rms.core.schedule.port.input.RecordTimeLogUseCase;
import aros.services.rms.core.schedule.port.input.RemoveScheduleFromWorkerUseCase;
import aros.services.rms.core.schedule.port.input.UpdateScheduleUseCase;
import aros.services.rms.core.schedule.port.output.ScheduleRepositoryPort;
import aros.services.rms.core.schedule.port.output.TimeLogRepositoryPort;
import aros.services.rms.core.schedule.port.output.WorkerScheduleAssignmentRepositoryPort;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScheduleConfigBeans {

  @Bean
  public CreateScheduleUseCase createScheduleUseCase(
      ScheduleRepositoryPort scheduleRepository, Logger logger) {
    return new CreateScheduleService(scheduleRepository, logger);
  }

  @Bean
  public UpdateScheduleUseCase updateScheduleUseCase(
      ScheduleRepositoryPort scheduleRepository, Logger logger) {
    return new UpdateScheduleService(scheduleRepository, logger);
  }

  @Bean
  public DeleteScheduleUseCase deleteScheduleUseCase(
      ScheduleRepositoryPort scheduleRepository,
      WorkerScheduleAssignmentRepositoryPort assignmentRepository,
      Logger logger) {
    return new DeleteScheduleService(scheduleRepository, assignmentRepository, logger);
  }

  @Bean
  public AssignScheduleToWorkerUseCase assignScheduleToWorkerUseCase(
      ScheduleRepositoryPort scheduleRepository,
      WorkerScheduleAssignmentRepositoryPort assignmentRepository,
      Logger logger) {
    return new AssignScheduleToWorkerService(scheduleRepository, assignmentRepository, logger);
  }

  @Bean
  public RemoveScheduleFromWorkerUseCase removeScheduleFromWorkerUseCase(
      WorkerScheduleAssignmentRepositoryPort assignmentRepository, Logger logger) {
    return new RemoveScheduleFromWorkerService(assignmentRepository, logger);
  }

  @Bean
  public GetWorkerShiftsUseCase getWorkerShiftsUseCase(
      WorkerScheduleAssignmentRepositoryPort assignmentRepository,
      ScheduleRepositoryPort scheduleRepository) {
    return new GetWorkerShiftsService(assignmentRepository, scheduleRepository);
  }

  @Bean
  public RecordTimeLogUseCase recordTimeLogUseCase(
      TimeLogRepositoryPort timeLogRepository,
      WorkerScheduleAssignmentRepositoryPort assignmentRepository,
      ScheduleRepositoryPort scheduleRepository,
      @Value("${app.timezone:America/Bogota}") String timezone) {
    return new RecordTimeLogService(
        timeLogRepository, assignmentRepository, scheduleRepository, ZoneId.of(timezone));
  }

  @Bean
  public GetTimeLogHistoryUseCase getTimeLogHistoryUseCase(
      TimeLogRepositoryPort timeLogRepository) {
    return new GetTimeLogHistoryService(timeLogRepository);
  }
}
