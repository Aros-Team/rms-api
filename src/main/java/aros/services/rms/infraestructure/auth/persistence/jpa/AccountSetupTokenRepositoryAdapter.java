/* (C) 2026 */
package aros.services.rms.infraestructure.auth.persistence.jpa;

import aros.services.rms.core.auth.domain.AccountSetupToken;
import aros.services.rms.core.auth.port.output.AccountSetupTokenRepositoryPort;
import aros.services.rms.core.user.domain.UserId;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/** Adapter for account setup token repository. */
@Repository
@RequiredArgsConstructor
@Transactional
public class AccountSetupTokenRepositoryAdapter implements AccountSetupTokenRepositoryPort {
  private final JpaAccountSetupTokenRepository internal;

  private final AccountSetupTokenMapper mapper;

  @Override
  public AccountSetupToken save(AccountSetupToken token) {
    return mapper.toDomain(internal.save(mapper.toEntity(token)));
  }

  @Override
  public Optional<AccountSetupToken> findByTokenHashAndUserId(String tokenHash, UserId userId) {
    return internal.findByTokenHashAndUserId(tokenHash, userId.value()).map(mapper::toDomain);
  }

  @Override
  public Optional<AccountSetupToken> findByTokenHash(String tokenHash) {
    return internal.findByTokenHash(tokenHash).map(mapper::toDomain);
  }

  @Override
  public void deleteByUserId(UserId userId) {
    internal.deleteByUserId(userId.value());
  }
}
