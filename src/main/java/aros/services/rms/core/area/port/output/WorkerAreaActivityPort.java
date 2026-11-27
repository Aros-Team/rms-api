/* (C) 2026 */

package aros.services.rms.core.area.port.output;

import aros.services.rms.core.area.domain.AreaActivity;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/** Persistence port for the activities a worker performs in each assigned area. */
public interface WorkerAreaActivityPort {

  /**
   * Loads the explicit worker activities.
   *
   * @param userId the worker identifier
   * @return area id -> activities
   */
  Map<Long, Set<AreaActivity>> findByUserId(Long userId);

  /**
   * Loads explicit worker activities for several users.
   *
   * @param userIds the worker identifiers
   * @return user id -> area id -> activities
   */
  Map<Long, Map<Long, Set<AreaActivity>>> findByUserIds(Collection<Long> userIds);

  /**
   * Replaces the worker's explicit activity assignments.
   *
   * @param userId the worker identifier
   * @param activitiesByArea area id -> activities
   */
  void replaceForUser(Long userId, Map<Long, Set<AreaActivity>> activitiesByArea);

  /**
   * Loads every activity configured on the given areas.
   *
   * @param areaIds the area identifiers
   * @return area id -> activities
   */
  Map<Long, Set<AreaActivity>> findAreaActivities(Collection<Long> areaIds);
}
