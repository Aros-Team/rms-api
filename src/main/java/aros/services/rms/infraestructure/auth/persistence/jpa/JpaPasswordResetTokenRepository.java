/* (C) 2026 */
package aros.services.rms.infraestructure.auth.persistence.jpa;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPasswordResetTokenRepository
    extends JpaRepository<PasswordResetTokenEntity, Long> {
  Optional<PasswordResetTokenEntity> findByTokenHash(String tokenHash);

  Optional<PasswordResetTokenEntity> findByTokenHashAndUserId(String tokenHash, Long userId);

  void deleteByUserId(Long userId);
}
