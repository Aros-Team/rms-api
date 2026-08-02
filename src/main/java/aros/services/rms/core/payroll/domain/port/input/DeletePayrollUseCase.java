/* (C) 2026 */

package aros.services.rms.core.payroll.domain.port.input;

/** Use case for deleting a payroll record. */
public interface DeletePayrollUseCase {

  /**
   * Deletes a payroll record by id.
   *
   * @param id the payroll id
   */
  void delete(Long id);
}
