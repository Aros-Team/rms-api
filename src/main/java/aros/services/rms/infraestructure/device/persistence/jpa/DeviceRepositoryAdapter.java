/* (C) 2026 */
package aros.services.rms.infraestructure.device.persistence.jpa;

import aros.services.rms.core.device.domain.Device;
import aros.services.rms.core.device.port.output.DeviceRepositoryPort;
import aros.services.rms.core.user.domain.UserId;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/** Repository adapter for device persistence. */
@Repository
@RequiredArgsConstructor
public class DeviceRepositoryAdapter implements DeviceRepositoryPort {
  private final JpaDeviceRepository internal;

  private final DevicePersistenceMapper mapper;

  @Override
  public Optional<Device> findByUserIdAndHash(UserId userId, String hash) {
    return internal.findByUserIdAndHash(userId.value(), hash).map(mapper::toDomain);
  }

  @Override
  public Device save(Device device) {
    return mapper.toDomain(internal.save(mapper.toEntity(device)));
  }
}
