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
   * Finds a page of global (admin) notifications, newest first.
   *
   * @param unreadOnly exclude read rows when true
   * @param pageable paging
   * @return matching rows
   */
  @Query(
      "select n from UserNotificationEntity n "
          + "where n.userId is null and (:unreadOnly = false or n.readAt is null) "
          + "order by n.createdAt desc, n.id desc")
  List<UserNotificationEntity> findGlobalPage(
      @Param("unreadOnly") boolean unreadOnly, Pageable pageable);

  /**
   * Finds a page of notifications targeted at one user, newest first.
   *
   * @param userId recipient id
   * @param unreadOnly exclude read rows when true
   * @param pageable paging
   * @return matching rows
   */
  @Query(
      "select n from UserNotificationEntity n "
          + "where n.userId = :userId and (:unreadOnly = false or n.readAt is null) "
          + "order by n.createdAt desc, n.id desc")
  List<UserNotificationEntity> findPageForUser(
      @Param("userId") Long userId, @Param("unreadOnly") boolean unreadOnly, Pageable pageable);

  /**
   * Counts global notifications.
   *
   * @param unreadOnly count only unread rows when true
   * @return row count
   */
  @Query(
      "select count(n) from UserNotificationEntity n "
          + "where n.userId is null and (:unreadOnly = false or n.readAt is null)")
  long countGlobal(@Param("unreadOnly") boolean unreadOnly);

  /**
   * Counts notifications targeted at one user.
   *
   * @param userId recipient id
   * @param unreadOnly count only unread rows when true
   * @return row count
   */
  @Query(
      "select count(n) from UserNotificationEntity n "
          + "where n.userId = :userId and (:unreadOnly = false or n.readAt is null)")
  long countForUser(@Param("userId") Long userId, @Param("unreadOnly") boolean unreadOnly);

  /**
   * Marks one global unread row as read.
   *
   * @param id notification id
   * @param readAt transition timestamp
   * @return number of rows updated
   */
  @Modifying
  @Query(
      "update UserNotificationEntity n set n.readAt = :readAt "
          + "where n.id = :id and n.userId is null and n.readAt is null")
  int markGlobalRead(@Param("id") Long id, @Param("readAt") Instant readAt);

  /**
   * Marks one row owned by a user as read.
   *
   * @param id notification id
   * @param userId recipient id
   * @param readAt transition timestamp
   * @return number of rows updated
   */
  @Modifying
  @Query(
      "update UserNotificationEntity n set n.readAt = :readAt "
          + "where n.id = :id and n.userId = :userId and n.readAt is null")
  int markReadForUser(
      @Param("id") Long id, @Param("userId") Long userId, @Param("readAt") Instant readAt);

  /**
   * Marks every unread global row as read.
   *
   * @param readAt transition timestamp
   * @return number of rows updated
   */
  @Modifying
  @Query(
      "update UserNotificationEntity n set n.readAt = :readAt "
          + "where n.userId is null and n.readAt is null")
  int markAllGlobalRead(@Param("readAt") Instant readAt);

  /**
   * Marks every unread row of one user as read.
   *
   * @param userId recipient id
   * @param readAt transition timestamp
   * @return number of rows updated
   */
  @Modifying
  @Query(
      "update UserNotificationEntity n set n.readAt = :readAt "
          + "where n.userId = :userId and n.readAt is null")
  int markAllReadForUser(@Param("userId") Long userId, @Param("readAt") Instant readAt);

  /** Deletes the global admin feed. */
  @Modifying
  @Query("delete from UserNotificationEntity n where n.userId is null")
  int deleteGlobal();
}
