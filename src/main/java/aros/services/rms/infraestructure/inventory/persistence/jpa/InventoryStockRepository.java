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

/** JPA repository for inventory stock. */
@Repository
public interface InventoryStockRepository extends JpaRepository<InventoryStockEntity, Long> {

  /**
   * Finds inventory stock by storage location ID.
   *
   * @param storageLocationId the storage location ID
   * @return the list of inventory stock entities
   */
  List<InventoryStockEntity> findByStorageLocationId(Long storageLocationId);

  /**
   * Finds inventory stock by supply variant ID and storage location ID.
   *
   * @param supplyVariantId the supply variant ID
   * @param storageLocationId the storage location ID
   * @return the inventory stock entity if found
   */
  Optional<InventoryStockEntity> findBySupplyVariantIdAndStorageLocationId(
      Long supplyVariantId, Long storageLocationId);

  /**
   * Finds inventory stock by variant and location with pessimistic lock.
   *
   * @param supplyVariantId the supply variant ID
   * @param storageLocationId the storage location ID
   * @return the inventory stock entity if found
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "SELECT s FROM InventoryStockEntity s "
          + "WHERE s.supplyVariant.id = :supplyVariantId "
          + "AND s.storageLocation.id = :storageLocationId")
  Optional<InventoryStockEntity> findByVariantAndLocationWithLock(
      @Param("supplyVariantId") Long supplyVariantId,
      @Param("storageLocationId") Long storageLocationId);
}
