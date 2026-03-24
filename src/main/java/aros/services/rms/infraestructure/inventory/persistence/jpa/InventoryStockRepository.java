/* (C) 2026 */
package aros.services.rms.infraestructure.inventory.persistence.jpa;

import aros.services.rms.infraestructure.inventory.persistence.InventoryStockEntity;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryStockRepository extends JpaRepository<InventoryStockEntity, Long> {

  List<InventoryStockEntity> findByStorageLocationId(Long storageLocationId);

  Optional<InventoryStockEntity> findBySupplyVariantIdAndStorageLocationId(
      Long supplyVariantId, Long storageLocationId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "SELECT s FROM InventoryStockEntity s "
          + "WHERE s.supplyVariant.id = :supplyVariantId "
          + "AND s.storageLocation.id = :storageLocationId")
  Optional<InventoryStockEntity> findByVariantAndLocationWithLock(
      @Param("supplyVariantId") Long supplyVariantId,
      @Param("storageLocationId") Long storageLocationId);
}
