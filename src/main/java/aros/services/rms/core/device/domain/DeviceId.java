package aros.services.rms.core.device.domain;

public record DeviceId (Long id) {
    public DeviceId {
        if (id != null && id <= 0) {
            throw new IllegalArgumentException("El id del dispositivo no puede ser negativo");
        }
    }
}
