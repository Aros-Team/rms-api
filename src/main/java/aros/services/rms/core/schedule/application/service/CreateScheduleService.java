package aros.services.rms.core.schedule.application.service;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.schedule.application.exception.ScheduleAlreadyExistsException;
import aros.services.rms.core.schedule.domain.DayOfWeek;
import aros.services.rms.core.schedule.domain.Schedule;
import aros.services.rms.core.schedule.domain.ScheduleShift;
import aros.services.rms.core.schedule.port.input.CreateScheduleUseCase;
import aros.services.rms.core.schedule.port.output.ScheduleRepositoryPort;
import java.time.LocalTime;
import java.util.List;

public class CreateScheduleService implements CreateScheduleUseCase {
  private final ScheduleRepositoryPort scheduleRepository;
  private final Logger logger;

  public CreateScheduleService(ScheduleRepositoryPort scheduleRepository, Logger logger) {
    this.scheduleRepository = scheduleRepository;
    this.logger = logger;
  }

  @Override
  public Schedule create(CreateScheduleInfo info) {
    if (scheduleRepository.existsByName(info.name())) {
      throw new ScheduleAlreadyExistsException("Schedule already exists with name: " + info.name());
    }

    List<ScheduleShift> shifts =
        info.shifts().stream()
            .map(
                s ->
                    new ScheduleShift(
                        DayOfWeek.valueOf(s.dayOfWeek()),
                        LocalTime.parse(s.startTime()),
                        LocalTime.parse(s.endTime())))
            .toList();

    Schedule schedule = new Schedule(info.name(), info.description(), shifts);
    Schedule saved = scheduleRepository.save(schedule);
    logger.info("Schedule created: id={}, name={}", saved.getId().value(), saved.getName());
    return saved;
  }
}
