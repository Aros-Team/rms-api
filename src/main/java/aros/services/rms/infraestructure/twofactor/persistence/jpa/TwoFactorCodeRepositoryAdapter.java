package aros.services.rms.infraestructure.twofactor.persistence.jpa;

import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import aros.services.rms.core.twofactor.domain.TwoFactorCode;
import aros.services.rms.core.twofactor.port.output.TwoFactorCodeRepositoryPort;
import aros.services.rms.core.user.domain.UserId;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TwoFactorCodeRepositoryAdapter implements TwoFactorCodeRepositoryPort {
    private final JpaTwoFactorCodeRepository internal;
    private final TwoFactorCodePersistenceMapper mapper;

    @Override
    public TwoFactorCode save(TwoFactorCode code) {
        return mapper.toDomain(internal.save(mapper.toEntity(code)));
    }

    @Override
    public Optional<TwoFactorCode> findValidCode(UserId userId, String codeHash) {
        return internal.findByUserIdAndCodeHashAndUsedAtIsNullAndExpiresAtAfter(
            userId.value(), codeHash, Instant.now()
        ).map(mapper::toDomain);
    }
}
