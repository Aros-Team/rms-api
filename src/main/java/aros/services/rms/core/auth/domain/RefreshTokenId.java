/* (C) 2026 */
package aros.services.rms.core.auth.domain;

public record RefreshTokenId(Long value) {
    public RefreshTokenId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("El ID no puede ser nulo o negativo");
        }
    }

    public static RefreshTokenId of(Long value) {
        return new RefreshTokenId(value);
    }
}
