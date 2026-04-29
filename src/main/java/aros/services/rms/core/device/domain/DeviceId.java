/* (C) 2026 */

package aros.services.rms.core.device.domain;

/** Value object representing the unique identifier for a device. */
public record DeviceId(Long id) {
  /** Validates the device ID. */
  public DeviceId {
    if (id != null && id <= 0) {
      throw new IllegalArgumentException("El id del dispositivo no puede ser negativo");
    }
  }
}
