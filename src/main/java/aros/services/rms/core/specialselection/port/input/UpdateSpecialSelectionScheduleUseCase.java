package aros.services.rms.core.specialselection.port.input;

import aros.services.rms.core.specialselection.domain.SpecialSelectionConfiguration;
import aros.services.rms.core.specialselection.domain.SpecialSelectionScheduleEntry;
import java.util.List;

/**
 * Input port for updating the availability schedule of an existing special selection configuration.
 */
public interface UpdateSpecialSelectionScheduleUseCase {
  /**
   * Replaces the schedule of the special selection associated with the given product.
   *
   * @param productId the product identifier
   * @param schedule the new schedule entries
   * @param changedBy the user performing the update
   * @return the updated configuration
   */
  SpecialSelectionConfiguration execute(
      Long productId, List<SpecialSelectionScheduleEntry> schedule, String changedBy);
}
