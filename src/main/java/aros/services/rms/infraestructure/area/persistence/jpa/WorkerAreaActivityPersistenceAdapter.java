/* (C) 2026 */

package aros.services.rms.infraestructure.area.persistence.jpa;

import aros.services.rms.core.area.domain.AreaActivity;
import aros.services.rms.core.area.port.output.WorkerAreaActivityPort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/** Reads and writes {@code worker_area_activities}. */
@Component
public class WorkerAreaActivityPersistenceAdapter implements WorkerAreaActivityPort {

  @PersistenceContext private EntityManager entityManager;

  /** {@inheritDoc} */
  @Override
  public Map<Long, Set<AreaActivity>> findByUserId(Long userId) {
    if (userId == null) {
      return Map.of();
    }
    return load("w.user_id = :id", Map.of("id", userId), false).getOrDefault(userId, Map.of());
  }

  /** {@inheritDoc} */
  @Override
  public Map<Long, Map<Long, Set<AreaActivity>>> findByUserIds(Collection<Long> userIds) {
    if (userIds == null || userIds.isEmpty()) {
      return Map.of();
    }
    return load("w.user_id IN :ids", Map.of("ids", userIds), false);
  }

  /** {@inheritDoc} */
  @Override
  public Map<Long, Set<AreaActivity>> findAreaActivities(Collection<Long> areaIds) {
    if (areaIds == null || areaIds.isEmpty()) {
      return Map.of();
    }
    String sql = "SELECT a.area_id, a.activity FROM area_activities a WHERE a.area_id IN :ids";
    List<?> rows =
        entityManager.createNativeQuery(sql).setParameter("ids", areaIds).getResultList();
    Map<Long, Set<AreaActivity>> result = new LinkedHashMap<>();
    for (Object raw : rows) {
      Object[] row = (Object[]) raw;
      result
          .computeIfAbsent(((Number) row[0]).longValue(), key -> new LinkedHashSet<>())
          .add(AreaActivity.valueOf(row[1].toString()));
    }
    return result;
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public void replaceForUser(Long userId, Map<Long, Set<AreaActivity>> activitiesByArea) {
    entityManager
        .createNativeQuery("DELETE FROM worker_area_activities WHERE user_id = :id")
        .setParameter("id", userId)
        .executeUpdate();
    if (activitiesByArea == null || activitiesByArea.isEmpty()) {
      return;
    }
    for (Map.Entry<Long, Set<AreaActivity>> entry : activitiesByArea.entrySet()) {
      if (entry.getKey() == null || entry.getValue() == null) {
        continue;
      }
      for (AreaActivity activity : entry.getValue()) {
        if (activity == null) {
          continue;
        }
        entityManager
            .createNativeQuery(
                "INSERT IGNORE INTO worker_area_activities (user_id, area_id, activity) "
                    + "VALUES (:userId, :areaId, :activity)")
            .setParameter("userId", userId)
            .setParameter("areaId", entry.getKey())
            .setParameter("activity", activity.name())
            .executeUpdate();
      }
    }
  }

  private Map<Long, Map<Long, Set<AreaActivity>>> load(
      String condition, Map<String, Object> params, boolean unused) {
    String sql =
        "SELECT w.user_id, w.area_id, w.activity FROM worker_area_activities w WHERE " + condition;
    var query = entityManager.createNativeQuery(sql);
    for (Map.Entry<String, Object> param : params.entrySet()) {
      query.setParameter(param.getKey(), param.getValue());
    }
    List<?> rows = query.getResultList();
    Map<Long, Map<Long, Set<AreaActivity>>> result = new LinkedHashMap<>();
    for (Object raw : rows) {
      Object[] row = (Object[]) raw;
      long userId = ((Number) row[0]).longValue();
      long areaId = ((Number) row[1]).longValue();
      result
          .computeIfAbsent(userId, key -> new LinkedHashMap<>())
          .computeIfAbsent(areaId, key -> new LinkedHashSet<>())
          .add(AreaActivity.valueOf(row[2].toString()));
    }
    return result;
  }
}
