package aros.services.rms.infraestructure.schedule.persistence.jpa;

import aros.services.rms.core.schedule.domain.TimeLog;
import aros.services.rms.core.schedule.port.output.TimeLogRepositoryPort;
import aros.services.rms.core.user.domain.UserId;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
@RequiredArgsConstructor
public class TimeLogRepositoryAdapter implements TimeLogRepositoryPort {

  private final TimeLogJpaRepository internal;
  private final TimeLogMapper mapper;

  @Override
  public TimeLog save(TimeLog timeLog) {
    TimeLogEntity entity = mapper.toEntity(timeLog);
    TimeLogEntity saved = internal.save(entity);
    return mapper.toDomain(saved);
  }

  @Override
  public List<TimeLog> findByWorkerIdAndDateRange(UserId workerId, Instant from, Instant to) {
    return internal.findByWorkerIdAndTimestampBetween(workerId.value(), from, to).stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Override
  public List<TimeLog> findAllByDateRange(Instant from, Instant to) {
    return internal.findByTimestampBetween(from, to).stream().map(mapper::toDomain).toList();
  }
}
