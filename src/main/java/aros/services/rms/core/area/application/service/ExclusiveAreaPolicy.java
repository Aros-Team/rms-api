/* (C) 2026 */

package aros.services.rms.core.area.application.service;

import aros.services.rms.core.area.application.exception.ExclusiveAreaAssignmentException;
import aros.services.rms.core.area.domain.Area;
import java.util.Collection;

/**
 * Enforces the exclusive-area rule shared by every worker assignment path.
 *
 * <p>An exclusive area can only be assigned on its own: a worker assigned to Cocina cannot also
 * belong to Servicio or Bar.
 */
public final class ExclusiveAreaPolicy {

  private ExclusiveAreaPolicy() {}

  /**
   * Validates that the assigned areas can coexist for one worker.
   *
   * @param areas the complete set of areas being assigned
   * @throws ExclusiveAreaAssignmentException when an exclusive area is combined with another
   */
  public static void assertAssignable(Collection<Area> areas) {
    if (areas == null || areas.size() <= 1) {
      return;
    }
    areas.stream()
        .filter(Area::isExclusive)
        .findFirst()
        .ifPresent(
            exclusive -> {
              throw new ExclusiveAreaAssignmentException(
                  "El área «"
                      + exclusive.getName()
                      + "» es exclusiva: un trabajador no puede tener otras áreas además de esa.");
            });
  }
}
