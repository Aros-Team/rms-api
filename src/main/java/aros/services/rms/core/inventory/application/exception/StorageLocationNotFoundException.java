/* (C) 2026 */

package aros.services.rms.core.inventory.application.exception;

/** Exception thrown when a storage location is not found. */
public class StorageLocationNotFoundException extends RuntimeException {

  /**
   * Creates exception by ID.
   *
   * @param id storage location ID
   */
  public StorageLocationNotFoundException(Long id) {
    super("Storage location not found: " + id);
  }

  /**
   * Creates exception by name.
   *
   * @param name storage location name
   */
  public StorageLocationNotFoundException(String name) {
    super("Storage location not found: " + name);
  }
}
