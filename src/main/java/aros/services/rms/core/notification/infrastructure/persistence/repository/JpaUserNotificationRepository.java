/* (C) 2026 */

package aros.services.rms.core.notification.infrastructure.persistence.repository;

import aros.services.rms.core.notification.infrastructure.persistence.entity.UserNotificationEntity;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Spring Data repository for user_notifications. */
public interface JpaUserNotificationRepository extends JpaRepository<UserNotificationEntity, Long> {

  /**
   * Whether a producer event was already recorded.
   *
   * @param dedupeKey producer idempotency key
   * @return true when a row with that key exists
   */
  boolean existsByDedupeKey(String dedupeKey);

  /**
   * Finds a page of notifications, newest first.
   *
   * @param unreadOnly exclude read rows when true
   * @param pageable paging
   * @return matching rows
   */
  @Query(
      "select n from UserNotificationEntity n "
          + "where (:unreadOnly = false or n.readAt is null) "
          + "order by n.createdAt desc, n.id desc")
  List<UserNotificationEntity> findPage(@Param("unreadOnly") boolean unreadOnly, Pageable pageable);

  /**
   * Counts notifications.
   *
   * @param unreadOnly count only unread rows when true
   * @return row count
   */
  @Query(
      "select count(n) from UserNotificationEntity n "
          + "where (:unreadOnly = false or n.readAt is null)")
  long countNotifications(@Param("unreadOnly") boolean unreadOnly);

  /**
   * Marks one unread row as read.
   *
   * @param id notification id
   * @param readAt transition timestamp
   * @return number of rows updated
   */
  @Modifying
  @Query(
      "update UserNotificationEntity n set n.readAt = :readAt "
          + "where n.id = :id and n.readAt is null")
  int markRead(@Param("id") Long id, @Param("readAt") Instant readAt);

  /**
   * Marks every unread row as read.
   *
   * @param readAt transition timestamp
   * @return number of rows updated
   */
  @Modifying
  @Query("update UserNotificationEntity n set n.readAt = :readAt where n.readAt is null")
  int markAllRead(@Param("readAt") Instant readAt);
}
