/* (C) 2026 */
package aros.services.rms.infraestructure.user.persistence.jpa;

import aros.services.rms.core.user.domain.UserRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JpaUserRepository extends JpaRepository<UserEntity, Long> {
  Optional<UserEntity> findByEmail(String email);

  @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.assignedAreas WHERE u.email = :email")
  Optional<UserEntity> findByEmailWithAreas(String email);

  boolean existsByDocumentOrEmail(String document, String email);

  long countByRole(UserRole role);

  List<UserEntity> findAll();
}
