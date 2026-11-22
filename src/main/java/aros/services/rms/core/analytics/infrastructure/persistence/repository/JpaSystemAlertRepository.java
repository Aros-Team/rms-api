/* (C) 2026 */

package aros.services.rms.core.analytics.infrastructure.persistence.repository;

import aros.services.rms.core.analytics.domain.AlertSeverity;
import aros.services.rms.core.analytics.domain.AlertStatus;
import aros.services.rms.core.analytics.domain.AlertType;
import aros.services.rms.core.analytics.infrastructure.persistence.entity.SystemAlertEntity;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Spring Data repository for the system_alerts entity. */
public interface JpaSystemAlertRepository extends JpaRepository<SystemAlertEntity, Long> {

  /**
   * Finds alerts matching the optional filters, newest first.
   *
   * @param status optional status
   * @param severity optional severity
   * @param bucket optional period bucket
   * @param fromKey optional inclusive period key
   * @param toKey optional inclusive period key
   * @param pageable paging
   * @return the matching alerts
   */
  @Query(
      "SELECT a FROM SystemAlertEntity a "
          + "WHERE (:status IS NULL OR a.status = :status) "
          + "AND (:severity IS NULL OR a.severity = :severity) "
          + "AND (:bucket IS NULL OR a.periodBucket = :bucket) "
          + "AND (:fromKey IS NULL OR a.periodKey >= :fromKey) "
          + "AND (:toKey IS NULL OR a.periodKey <= :toKey) "
          + "ORDER BY a.detectedAt DESC, a.id DESC")
  List<SystemAlertEntity> findFiltered(
      @Param("status") AlertStatus status,
      @Param("severity") AlertSeverity severity,
      @Param("bucket") String bucket,
      @Param("fromKey") String fromKey,
      @Param("toKey") String toKey,
      Pageable pageable);

  /**
   * Counts alerts matching the optional filters.
   *
   * @param status optional status
   * @param severity optional severity
   * @param bucket optional period bucket
   * @param fromKey optional inclusive period key
   * @param toKey optional inclusive period key
   * @return the number of matches
   */
  @Query(
      "SELECT COUNT(a) FROM SystemAlertEntity a "
          + "WHERE (:status IS NULL OR a.status = :status) "
          + "AND (:severity IS NULL OR a.severity = :severity) "
          + "AND (:bucket IS NULL OR a.periodBucket = :bucket) "
          + "AND (:fromKey IS NULL OR a.periodKey >= :fromKey) "
          + "AND (:toKey IS NULL OR a.periodKey <= :toKey)")
  long countFiltered(
      @Param("status") AlertStatus status,
      @Param("severity") AlertSeverity severity,
      @Param("bucket") String bucket,
      @Param("fromKey") String fromKey,
      @Param("toKey") String toKey);

  /**
   * Checks whether a rule already produced an alert for the same metric and period.
   *
   * @param type the alert type
   * @param metric the metric name
   * @param periodKey the period key
   * @return true when the alert already exists
   */
  boolean existsByTypeAndMetricAndPeriodKey(AlertType type, String metric, String periodKey);

  /**
   * Marks an alert read.
   *
   * @param id the alert id
   * @param readAt the read timestamp
   */
  @Modifying
  @Query(
      "UPDATE SystemAlertEntity a SET a.status = :readStatus, a.readAt = :readAt "
          + "WHERE a.id = :id AND a.status = :openStatus")
  void markRead(
      @Param("id") Long id,
      @Param("readStatus") AlertStatus readStatus,
      @Param("openStatus") AlertStatus openStatus,
      @Param("readAt") Instant readAt);
}
