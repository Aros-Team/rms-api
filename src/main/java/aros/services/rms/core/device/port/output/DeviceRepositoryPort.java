package aros.services.rms.core.device.port.output;

import java.util.Optional;

import aros.services.rms.core.device.domain.Device;
import aros.services.rms.core.user.domain.UserId;

public interface DeviceRepositoryPort {
    public Optional<Device> findByUserIdAndHash(UserId userId, String hash);
    public Device save(Device device);
}
