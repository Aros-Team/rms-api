package aros.services.rms.infraestructure.device.persistence.jpa;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaDeviceRepository extends JpaRepository<DeviceEntity, Long> {
    Optional<DeviceEntity> findByUserIdAndHash(Long userId, String hash);
}
