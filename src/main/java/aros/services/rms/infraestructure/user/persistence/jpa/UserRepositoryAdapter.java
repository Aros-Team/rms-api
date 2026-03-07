package aros.services.rms.infraestructure.user.persistence.jpa;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import aros.services.rms.core.user.domain.User;
import aros.services.rms.core.user.port.output.UserRepositoryPort;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {
    @Autowired
    private JpaUserRepository internal;

    @Autowired
    private UserPersistenceMapper userMapper;

    @Override
    public Optional<User> findByEmail(String email) {
        return this.internal.findByEmail(email).map((u) -> userMapper.toDomain(u));
    }
}