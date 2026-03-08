/* (C) 2026 */
package aros.services.rms.core.area.port.output;

import java.util.List;

import aros.services.rms.core.area.domain.Area;
import aros.services.rms.core.area.domain.AreaId;

public interface AreaRepositoryPort {
  List<Area> findByIdIn(List<AreaId> ids);
}
