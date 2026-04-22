/* (C) 2026 */
package aros.services.rms.infraestructure.user.persistence.jpa;

import aros.services.rms.core.user.domain.UserRole;
import aros.services.rms.core.user.domain.UserStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaUserRepository extends JpaRepository<UserEntity, Long> {
  Optional<UserEntity> findByEmail(String email);

  @Query(
      "SELECT u FROM UserEntity u LEFT JOIN FETCH u.assignedAreas WHERE u.email = :email AND u.deletedAt IS NULL")
  Optional<UserEntity> findByEmailWithAreas(String email);

  boolean existsByDocumentOrEmail(String document, String email);

  @Query(
      "SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END "
          + "FROM UserEntity u WHERE u.document = :document AND u.email = :email AND u.deletedAt IS NULL")
  boolean existsByDocumentAndEmailAndDeletedAtIsNull(
      @Param("document") String document, @Param("email") String email);

  long countByRole(UserRole role);

  List<UserEntity> findAllByDeletedAtIsNull();

  List<UserEntity> findByStatusAndDeletedAtIsNull(UserStatus status);

  List<UserEntity> findByRoleAndStatusAndDeletedAtIsNull(UserRole role, UserStatus status);
}
