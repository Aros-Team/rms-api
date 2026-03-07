package aros.services.rms.infraestructure.auth.bcrypt;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import aros.services.rms.core.auth.port.output.PasswordEncoderPort;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BcryptPasswordEncoderAdapter implements PasswordEncoderPort {
    
    private final PasswordEncoder passwordEncoder;

    @Override
    public String encode(String password) {
        return passwordEncoder.encode(password);
    }

    @Override
    public Boolean validate(String password, String encoded) {
        return passwordEncoder.matches(password, encoded);
    }
}
