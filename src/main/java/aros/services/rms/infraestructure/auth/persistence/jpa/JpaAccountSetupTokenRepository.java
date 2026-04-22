/* (C) 2026 */
package aros.services.rms.infraestructure.auth.persistence.jpa;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaAccountSetupTokenRepository
    extends JpaRepository<AccountSetupTokenEntity, Long> {
  Optional<AccountSetupTokenEntity> findByTokenHash(String tokenHash);

  Optional<AccountSetupTokenEntity> findByTokenHashAndUserId(String tokenHash, Long userId);

  void deleteByUserId(Long userId);
}
