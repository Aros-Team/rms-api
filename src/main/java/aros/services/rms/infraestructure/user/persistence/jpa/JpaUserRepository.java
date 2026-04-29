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
  /** Finds a user by email. */
  Optional<UserEntity> findByEmail(String email);

  /** Finds a user by email with assigned areas. */
  @Query(
      "SELECT u FROM UserEntity u LEFT JOIN FETCH u.assignedAreas WHERE u.email = :email AND u.deletedAt IS NULL")
  Optional<UserEntity> findByEmailWithAreas(String email);

  /** Checks if a user exists by document or email. */
  boolean existsByDocumentOrEmail(String document, String email);

  /** Checks if a user exists by document, email and not deleted. */
  @Query(
      "SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END "
          + "FROM UserEntity u WHERE u.document = :document AND u.email = :email AND u.deletedAt IS NULL")
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
}
