package aros.services.rms.core.device.domain;

import aros.services.rms.core.user.domain.UserId;

public class Device {
    private DeviceId id;
    private UserId userId;
    private String hash;

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
