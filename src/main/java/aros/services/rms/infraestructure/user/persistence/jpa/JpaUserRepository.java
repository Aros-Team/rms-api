/* (C) 2026 */

package aros.services.rms.infraestructure.user.persistence.jpa;

import aros.services.rms.core.user.domain.UserRole;
import aros.services.rms.core.user.domain.UserStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** JPA repository for UserEntity. */
public interface JpaUserRepository extends JpaRepository<UserEntity, Long> {
  /** Finds an active (non-deleted) user by email. */
  @Query("SELECT u FROM UserEntity u WHERE u.email = :email AND u.deletedAt IS NULL")
  Optional<UserEntity> findByEmail(@Param("email") String email);

  /** Finds a user by email with assigned areas. */
  @Query(
      "SELECT u FROM UserEntity u LEFT JOIN FETCH u.assignedAreas "
          + "WHERE u.email = :email AND u.deletedAt IS NULL")
  Optional<UserEntity> findByEmailWithAreas(String email);

  /** Checks if a user exists by document or email (including deleted). */
  boolean existsByDocumentOrEmail(String document, String email);

  /** Checks if a non-deleted user exists by document or email. */
  @Query(
      "SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END "
          + "FROM UserEntity u "
          + "WHERE (u.document = :document OR u.email = :email) AND u.deletedAt IS NULL")
  boolean existsActiveByDocumentOrEmail(
      @Param("document") String document, @Param("email") String email);

  /** Checks if a user exists by document, email and not deleted. */
  @Query(
      "SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END "
          + "FROM UserEntity u "
          + "WHERE u.document = :document AND u.email = :email AND u.deletedAt IS NULL")
  boolean existsByDocumentAndEmailAndDeletedAtIsNull(
      @Param("document") String document, @Param("email") String email);

  /** Counts users by role. */
  long countByRole(UserRole role);

  /** Finds all non-deleted users. */
  List<UserEntity> findAllByDeletedAtIsNull();

  /** Finds users by status that are not deleted. */
  List<UserEntity> findByStatusAndDeletedAtIsNull(UserStatus status);

  /** Finds users by role and status that are not deleted. */
  List<UserEntity> findByRoleAndStatusAndDeletedAtIsNull(UserRole role, UserStatus status);

  /** Finds users by role that are not deleted. */
  List<UserEntity> findByRoleAndDeletedAtIsNull(UserRole role);

  /**
   * Finds active (non-deleted) users assigned to a specific area.
   *
   * @param areaId the area identifier
   * @return list of distinct active users in that area
   */
  @Query(
      "SELECT DISTINCT u FROM UserEntity u JOIN u.assignedAreas a "
          + "WHERE a.id = :areaId AND u.deletedAt IS NULL")
  List<UserEntity> findActiveByAreaId(@Param("areaId") Long areaId);
}
