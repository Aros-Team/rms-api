/* (C) 2026 */

package aros.services.rms.core.analytics.application.service;

import aros.services.rms.core.analytics.domain.exception.AlertNotFoundException;
import aros.services.rms.core.analytics.domain.port.in.MarkAlertReadUseCase;
import aros.services.rms.core.analytics.domain.port.out.SystemAlertRepositoryPort;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Idempotently marks an alert as read. */
@Service
@RequiredArgsConstructor
public class MarkAlertReadService implements MarkAlertReadUseCase {

  private final SystemAlertRepositoryPort alertRepositoryPort;
  private final Clock clock;

  /** {@inheritDoc} */
  @Override
  @Transactional
  public void markRead(Long id) {
    alertRepositoryPort.findById(id).orElseThrow(() -> new AlertNotFoundException(id));
    alertRepositoryPort.markRead(id, Instant.now(clock));
  }
}
