/* (C) 2026 */
package aros.services.rms.infraestructure.device.persistence.jpa;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for devices. */
public interface JpaDeviceRepository extends JpaRepository<DeviceEntity, Long> {
  /**
   * Finds a device by user ID and hash.
   *
   * @param userId the user ID
   * @param hash the device hash
   * @return the device entity if found
   */
  Optional<DeviceEntity> findByUserIdAndHash(Long userId, String hash);
}
