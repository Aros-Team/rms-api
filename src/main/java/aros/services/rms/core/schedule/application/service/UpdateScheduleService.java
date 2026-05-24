package aros.services.rms.core.schedule.application.service;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.schedule.application.exception.ScheduleAlreadyExistsException;
import aros.services.rms.core.schedule.application.exception.ScheduleNotFoundException;
import aros.services.rms.core.schedule.domain.DayOfWeek;
import aros.services.rms.core.schedule.domain.Schedule;
import aros.services.rms.core.schedule.domain.ScheduleId;
import aros.services.rms.core.schedule.domain.ScheduleShift;
import aros.services.rms.core.schedule.port.input.UpdateScheduleUseCase;
import aros.services.rms.core.schedule.port.output.ScheduleRepositoryPort;
import java.time.LocalTime;
import java.util.List;

public class UpdateScheduleService implements UpdateScheduleUseCase {
  private final ScheduleRepositoryPort scheduleRepository;
  private final Logger logger;

  public UpdateScheduleService(ScheduleRepositoryPort scheduleRepository, Logger logger) {
    this.scheduleRepository = scheduleRepository;
    this.logger = logger;
  }

  @Override
  public Schedule update(ScheduleId id, UpdateScheduleInfo info) {
    Schedule existing =
        scheduleRepository
            .findById(id)
            .orElseThrow(
                () -> new ScheduleNotFoundException("Schedule not found with id: " + id.value()));

    if (!existing.getName().equals(info.name()) && scheduleRepository.existsByName(info.name())) {
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

    existing.setName(info.name());
    existing.setDescription(info.description());
    existing.setShifts(shifts);

    Schedule saved = scheduleRepository.save(existing);
    logger.info("Schedule updated: id={}, name={}", saved.getId().value(), saved.getName());
    return saved;
  }
}
