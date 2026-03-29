/* (C) 2026 */
package aros.services.rms.core.inventory.application.exception;

/** Exception thrown when a storage location is not found. */
public class StorageLocationNotFoundException extends RuntimeException {

  public StorageLocationNotFoundException(Long id) {
    super("Storage location not found: " + id);
  }

  public StorageLocationNotFoundException(String name) {
    super("Storage location not found: " + name);
  }
}
