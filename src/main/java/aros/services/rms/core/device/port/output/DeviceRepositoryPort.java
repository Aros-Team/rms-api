/* (C) 2026 */

package aros.services.rms.core.device.port.output;

import aros.services.rms.core.device.domain.Device;
import aros.services.rms.core.user.domain.UserId;
import java.util.Optional;

/** Output port for device persistence operations. */
public interface DeviceRepositoryPort {
  /**
   * Finds a device by user ID and hash.
   *
   * @param userId the user ID
   * @param hash the device hash
   * @return optional device
   */
  Optional<Device> findByUserIdAndHash(UserId userId, String hash);

  /**
   * Saves a device.
   *
   * @param device the device to save
   * @return the saved device
   */
  Device save(Device device);
}
