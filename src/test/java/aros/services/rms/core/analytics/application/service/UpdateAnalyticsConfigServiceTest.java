/* (C) 2026 */

package aros.services.rms.core.analytics.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.analytics.domain.AnalyticsConfig;
import aros.services.rms.core.analytics.domain.exception.AnalyticsConfigNotFoundException;
import aros.services.rms.core.analytics.domain.exception.InvalidAnalyticsConfigException;
import aros.services.rms.core.analytics.domain.port.in.UpdateAnalyticsConfigUseCase.UpdateAnalyticsConfigCommand;
import aros.services.rms.core.analytics.domain.port.out.AnalyticsConfigRepositoryPort;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Unit tests for {@link UpdateAnalyticsConfigService}. Validates threshold rules, time-window
 * ordering, singleton identifier preservation, update timestamp stamping, and exception
 * propagation.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UpdateAnalyticsConfigServiceTest {

  private static final Clock FIXED_CLOCK =
      Clock.fixed(Instant.parse("2026-07-17T00:00:00Z"), ZoneOffset.UTC);

  @Mock private AnalyticsConfigRepositoryPort repo;

  // The service is built fresh in each test with a deterministic Clock so update timestamps are
  // reproducible. Constructor signature matches the production wiring (repo, clock).
  private UpdateAnalyticsConfigService serviceWithFixedClock() {
    return new UpdateAnalyticsConfigService(repo, FIXED_CLOCK);
  }

  // ---------------------------------------------------------------------------
  // U-01: happy path persists and bumps updatedAt from injected Clock
  // ---------------------------------------------------------------------------

  @Test
  void update_happy_path_persists_and_bumps_updatedAt() {
    UpdateAnalyticsConfigService svc = serviceWithFixedClock();
    AnalyticsConfig current = AnalyticsConfig.defaults(LocalDateTime.parse("2026-01-01T00:00:00"));
    when(repo.findSingleton()).thenReturn(current);

    UpdateAnalyticsConfigCommand cmd = validCommand(7L);
    when(repo.save(any(AnalyticsConfig.class))).thenAnswer(inv -> inv.getArgument(0));

    AnalyticsConfig result = svc.update(cmd);

    assertEquals(
        LocalDateTime.now(FIXED_CLOCK),
        result.updatedAt(),
        "updatedAt must be stamped from the injected Clock");
    assertEquals(7L, result.updatedBy(), "updatedBy must be propagated from the command");
  }

  // ---------------------------------------------------------------------------
  // U-02..U-04: negative thresholds throw InvalidAnalyticsConfigException
  // ---------------------------------------------------------------------------

  static Stream<Arguments> negativeThresholdCases() {
    return Stream.of(
        Arguments.of("foodCost", bigDecimal(-0.01), bigDecimal(0), bigDecimal(0)),
        Arguments.of("laborCost", bigDecimal(0), bigDecimal(-0.01), bigDecimal(0)),
        Arguments.of("salesDrop", bigDecimal(0), bigDecimal(0), bigDecimal(-0.01)));
  }

  @ParameterizedTest(name = "should_reject_{0}_negative_threshold")
  @MethodSource("negativeThresholdCases")
  void should_reject_negative_threshold(
      String caseName, BigDecimal food, BigDecimal labor, BigDecimal sales) {
    UpdateAnalyticsConfigService svc = serviceWithFixedClock();
    UpdateAnalyticsConfigCommand cmd =
        new UpdateAnalyticsConfigCommand(
            LocalTime.of(11, 0),
            LocalTime.of(23, 0),
            LocalTime.of(11, 0),
            LocalTime.of(15, 0),
            LocalTime.of(18, 0),
            LocalTime.of(23, 0),
            food,
            labor,
            sales,
            7L);

    assertThrows(InvalidAnalyticsConfigException.class, () -> svc.update(cmd));
    verify(repo, never()).save(any());
  }

  // ---------------------------------------------------------------------------
  // U-05..U-07: invalid time ordering throws InvalidAnalyticsConfigException
  // ---------------------------------------------------------------------------

  @Test
  void update_defaultOpen_equals_defaultClose_throws() {
    UpdateAnalyticsConfigService svc = serviceWithFixedClock();
    UpdateAnalyticsConfigCommand cmd =
        new UpdateAnalyticsConfigCommand(
            LocalTime.of(11, 0),
            LocalTime.of(11, 0),
            LocalTime.of(11, 0),
            LocalTime.of(15, 0),
            LocalTime.of(18, 0),
            LocalTime.of(23, 0),
            bigDecimal(2),
            bigDecimal(3),
            bigDecimal(10),
            7L);

    assertThrows(InvalidAnalyticsConfigException.class, () -> svc.update(cmd));
    verify(repo, never()).save(any());
  }

  @Test
  void update_defaultOpen_after_defaultClose_throws() {
    UpdateAnalyticsConfigService svc = serviceWithFixedClock();
    UpdateAnalyticsConfigCommand cmd =
        new UpdateAnalyticsConfigCommand(
            LocalTime.of(23, 0),
            LocalTime.of(11, 0),
            LocalTime.of(11, 0),
            LocalTime.of(15, 0),
            LocalTime.of(18, 0),
            LocalTime.of(23, 0),
            bigDecimal(2),
            bigDecimal(3),
            bigDecimal(10),
            7L);

    assertThrows(InvalidAnalyticsConfigException.class, () -> svc.update(cmd));
    verify(repo, never()).save(any());
  }

  @Test
  void update_lunchStart_equals_lunchEnd_throws() {
    UpdateAnalyticsConfigService svc = serviceWithFixedClock();
    UpdateAnalyticsConfigCommand cmd =
        new UpdateAnalyticsConfigCommand(
            LocalTime.of(11, 0),
            LocalTime.of(23, 0),
            LocalTime.of(12, 0),
            LocalTime.of(12, 0),
            LocalTime.of(18, 0),
            LocalTime.of(23, 0),
            bigDecimal(2),
            bigDecimal(3),
            bigDecimal(10),
            7L);

    assertThrows(InvalidAnalyticsConfigException.class, () -> svc.update(cmd));
    verify(repo, never()).save(any());
  }

  @Test
  void update_dinnerStart_equals_dinnerEnd_throws() {
    UpdateAnalyticsConfigService svc = serviceWithFixedClock();
    UpdateAnalyticsConfigCommand cmd =
        new UpdateAnalyticsConfigCommand(
            LocalTime.of(11, 0),
            LocalTime.of(23, 0),
            LocalTime.of(11, 0),
            LocalTime.of(15, 0),
            LocalTime.of(20, 0),
            LocalTime.of(20, 0),
            bigDecimal(2),
            bigDecimal(3),
            bigDecimal(10),
            7L);

    assertThrows(InvalidAnalyticsConfigException.class, () -> svc.update(cmd));
    verify(repo, never()).save(any());
  }

  // ---------------------------------------------------------------------------
  // U-08: service preserves id and updatedBy from the singleton row + command
  // ---------------------------------------------------------------------------

  @Test
  void update_preserves_id_and_updatedBy_from_command() {
    UpdateAnalyticsConfigService svc = serviceWithFixedClock();
    AnalyticsConfig current = AnalyticsConfig.defaults(LocalDateTime.parse("2026-01-01T00:00:00"));
    when(repo.findSingleton()).thenReturn(current);

    UpdateAnalyticsConfigCommand cmd = validCommand(42L);
    ArgumentCaptor<AnalyticsConfig> captor = ArgumentCaptor.forClass(AnalyticsConfig.class);
    when(repo.save(any(AnalyticsConfig.class))).thenAnswer(inv -> inv.getArgument(0));

    svc.update(cmd);

    verify(repo).save(captor.capture());
    AnalyticsConfig saved = captor.getValue();
    assertEquals(1, saved.id(), "singleton id must be preserved");
    assertEquals(42L, saved.updatedBy(), "updatedBy must come from the command");
  }

  // ---------------------------------------------------------------------------
  // U-09: service propagates AnalyticsConfigNotFoundException
  // ---------------------------------------------------------------------------

  @Test
  void should_throw_when_config_not_found() {
    UpdateAnalyticsConfigService svc = serviceWithFixedClock();
    when(repo.findSingleton()).thenThrow(new AnalyticsConfigNotFoundException());

    UpdateAnalyticsConfigCommand cmd = validCommand(7L);

    assertThrows(AnalyticsConfigNotFoundException.class, () -> svc.update(cmd));
    verify(repo, never()).save(any());
  }

  // ---------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------

  private static UpdateAnalyticsConfigCommand validCommand(Long updatedBy) {
    return new UpdateAnalyticsConfigCommand(
        LocalTime.of(11, 0),
        LocalTime.of(23, 0),
        LocalTime.of(11, 0),
        LocalTime.of(15, 0),
        LocalTime.of(18, 0),
        LocalTime.of(23, 0),
        bigDecimal(2),
        bigDecimal(3),
        bigDecimal(10),
        updatedBy);
  }

  private static BigDecimal bigDecimal(double v) {
    return BigDecimal.valueOf(v);
  }
}
