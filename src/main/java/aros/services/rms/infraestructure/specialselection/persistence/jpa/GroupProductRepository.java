package aros.services.rms.infraestructure.specialselection.persistence.jpa;

import aros.services.rms.infraestructure.specialselection.persistence.GroupProductEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA repository for group-product associations in special selections. */
@Repository
public interface GroupProductRepository
    extends JpaRepository<GroupProductEntity, GroupProductEntity.GroupProductId> {

  /** Finds all group-product links for a set of group IDs. */
  List<GroupProductEntity> findByGroupIdIn(List<Long> groupIds);

  /** Finds all product IDs linked to a single group. */
  List<GroupProductEntity> findByGroupId(Long groupId);

  /** Deletes all links for a single group. */
  void deleteByGroupId(Long groupId);

  /** Deletes all links for a set of groups. */
  void deleteByGroupIdIn(List<Long> groupIds);
}
