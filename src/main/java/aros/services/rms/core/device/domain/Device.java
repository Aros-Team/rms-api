/* (C) 2026 */

package aros.services.rms.core.device.domain;

import aros.services.rms.core.user.domain.UserId;

/** Domain model representing a trusted device for 2FA bypass. */
public class Device {
  private DeviceId id;
  private UserId userId;
  private String hash;

  /**
   * Creates a device.
   *
   * @param id device identifier
   * @param userId user identifier
   * @param hash device hash for 2FA bypass
   */
  public Device(DeviceId id, UserId userId, String hash) {
    this.id = id;
    this.userId = userId;
    this.hash = hash;
  }

  public DeviceId getId() {
    return id;
  }

  public UserId getUserId() {
    return userId;
  }

  public String getHash() {
    return hash;
  }
}
