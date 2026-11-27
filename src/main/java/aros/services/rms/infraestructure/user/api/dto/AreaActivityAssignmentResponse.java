/* (C) 2026 */

package aros.services.rms.infraestructure.user.api.dto;

import aros.services.rms.core.area.domain.AreaActivity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Set;

/** Activities a worker performs in one assigned area. */
@Schema(description = "Activities assigned to a worker in one area")
public record AreaActivityAssignmentResponse(
    @Schema(description = "Area ID", example = "3") Long areaId,
    @Schema(description = "Activities", example = "[\"PREPARATION\",\"SERVE\"]")
        List<String> activities) {

  /**
   * Maps the internal set to a stable response.
   *
   * @param areaId the area id
   * @param activities the assigned activities
   * @return the response row
   */
  public static AreaActivityAssignmentResponse of(Long areaId, Set<AreaActivity> activities) {
    return new AreaActivityAssignmentResponse(
        areaId,
        activities == null ? List.of() : activities.stream().map(Enum::name).sorted().toList());
  }
}
