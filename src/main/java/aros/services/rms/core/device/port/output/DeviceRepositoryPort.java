/* (C) 2026 */
package aros.services.rms.core.device.port.output;

import aros.services.rms.core.device.domain.Device;
import aros.services.rms.core.user.domain.UserId;
import java.util.Optional;

public interface DeviceRepositoryPort {
  Optional<Device> findByUserIdAndHash(UserId userId, String hash);

  Device save(Device device);
}
