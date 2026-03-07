package aros.services.rms.infraestructure.twofactor.persistence.jpa;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaTwoFactorCodeRepository extends JpaRepository<TwoFactorCodeEntity, Long> {
    Optional<TwoFactorCodeEntity> findByUserIdAndCodeHashAndUsedAtIsNullAndExpiresAtAfter(
        Long userId, String codeHash, Instant now);
}
