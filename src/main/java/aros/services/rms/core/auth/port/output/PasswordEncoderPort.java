package aros.services.rms.core.auth.port.output;

public interface PasswordEncoderPort {
    public String encode(String password);

    public Boolean validate(String password, String encoded);
}
