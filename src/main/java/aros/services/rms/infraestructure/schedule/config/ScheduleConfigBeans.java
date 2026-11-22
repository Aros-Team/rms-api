package aros.services.rms.infraestructure.schedule.config;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.schedule.application.service.AssignScheduleToWorkerService;
import aros.services.rms.core.schedule.application.service.ClockAttendanceService;
import aros.services.rms.core.schedule.application.service.CreateScheduleService;
import aros.services.rms.core.schedule.application.service.DeleteScheduleService;
import aros.services.rms.core.schedule.application.service.GetTimeLogHistoryService;
import aros.services.rms.core.schedule.application.service.GetWorkerShiftsService;
import aros.services.rms.core.schedule.application.service.RecordTimeLogService;
import aros.services.rms.core.schedule.application.service.RemoveScheduleFromWorkerService;
import aros.services.rms.core.schedule.application.service.UpdateScheduleService;
import aros.services.rms.core.schedule.application.service.WorkerShiftResolver;
import aros.services.rms.core.schedule.port.input.AssignScheduleToWorkerUseCase;
import aros.services.rms.core.schedule.port.input.ClockInUseCase;
import aros.services.rms.core.schedule.port.input.ClockOutUseCase;
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
import aros.services.rms.core.systemconfig.domain.port.output.BusinessTimeZoneProvider;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Spring configuration that wires schedule use case beans. */
@Configuration
public class ScheduleConfigBeans {

  /** Creates the CreateScheduleUseCase bean. */
  @Bean
  public CreateScheduleUseCase createScheduleUseCase(
      ScheduleRepositoryPort scheduleRepository, Logger logger) {
    return new CreateScheduleService(scheduleRepository, logger);
  }

  /** Creates the UpdateScheduleUseCase bean. */
  @Bean
  public UpdateScheduleUseCase updateScheduleUseCase(
      ScheduleRepositoryPort scheduleRepository, Logger logger) {
    return new UpdateScheduleService(scheduleRepository, logger);
  }

  /** Creates the DeleteScheduleUseCase bean. */
  @Bean
  public DeleteScheduleUseCase deleteScheduleUseCase(
      ScheduleRepositoryPort scheduleRepository,
      WorkerScheduleAssignmentRepositoryPort assignmentRepository,
      Logger logger) {
    return new DeleteScheduleService(scheduleRepository, assignmentRepository, logger);
  }

  /** Creates the AssignScheduleToWorkerUseCase bean. */
  @Bean
  public AssignScheduleToWorkerUseCase assignScheduleToWorkerUseCase(
      ScheduleRepositoryPort scheduleRepository,
      WorkerScheduleAssignmentRepositoryPort assignmentRepository,
      Logger logger) {
    return new AssignScheduleToWorkerService(scheduleRepository, assignmentRepository, logger);
  }

  /** Creates the RemoveScheduleFromWorkerUseCase bean. */
  @Bean
  public RemoveScheduleFromWorkerUseCase removeScheduleFromWorkerUseCase(
      WorkerScheduleAssignmentRepositoryPort assignmentRepository, Logger logger) {
    return new RemoveScheduleFromWorkerService(assignmentRepository, logger);
  }

  /** Creates the GetWorkerShiftsUseCase bean. */
  @Bean
  public GetWorkerShiftsUseCase getWorkerShiftsUseCase(
      WorkerScheduleAssignmentRepositoryPort assignmentRepository,
      ScheduleRepositoryPort scheduleRepository) {
    return new GetWorkerShiftsService(assignmentRepository, scheduleRepository);
  }

  /** Creates the WorkerShiftResolver bean. */
  @Bean
  public WorkerShiftResolver workerShiftResolver(
      WorkerScheduleAssignmentRepositoryPort assignmentRepository,
      ScheduleRepositoryPort scheduleRepository,
      BusinessTimeZoneProvider timeZoneProvider) {
    return new WorkerShiftResolver(
        assignmentRepository, scheduleRepository, timeZoneProvider.getZoneId());
  }

  /** Creates the RecordTimeLogUseCase bean used by login. */
  @Bean
  public RecordTimeLogUseCase recordTimeLogUseCase(
      TimeLogRepositoryPort timeLogRepository, WorkerShiftResolver workerShiftResolver) {
    return new RecordTimeLogService(timeLogRepository, workerShiftResolver, Clock.systemUTC());
  }

  /** Creates the explicit clock-in service used by the worker attendance routes. */
  @Bean
  public ClockInUseCase clockInUseCase(
      TimeLogRepositoryPort timeLogRepository,
      WorkerShiftResolver workerShiftResolver,
      Logger logger) {
    return new ClockAttendanceService(
        timeLogRepository, workerShiftResolver, logger, Clock.systemUTC());
  }

  /** Creates the explicit clock-out service used by the worker attendance routes. */
  @Bean
  public ClockOutUseCase clockOutUseCase(
      TimeLogRepositoryPort timeLogRepository,
      WorkerShiftResolver workerShiftResolver,
      Logger logger) {
    return new ClockAttendanceService(
        timeLogRepository, workerShiftResolver, logger, Clock.systemUTC());
  }

  /** Creates the GetTimeLogHistoryUseCase bean. */
  @Bean
  public GetTimeLogHistoryUseCase getTimeLogHistoryUseCase(
      TimeLogRepositoryPort timeLogRepository, WorkerShiftResolver workerShiftResolver) {
    return new GetTimeLogHistoryService(timeLogRepository, workerShiftResolver);
  }
}
