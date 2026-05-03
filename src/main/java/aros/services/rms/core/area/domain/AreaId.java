/* (C) 2026 */

package aros.services.rms.core.area.domain;

/**
 * Value object representing the unique identifier for an area. Ensures non-null validation at
 * construction time.
 */
public record AreaId(Long value) {
  /**
   * Canonical constructor with validation.
   *
   * @param value the area identifier value
   * @throws IllegalArgumentException if value is null
   */
  public AreaId {
    if (value == null) {
      throw new IllegalArgumentException("El ID del area no puede ser nulo");
    }
  }

  /**
   * Factory method to create an AreaId from a Long value.
   *
   * @param value the area identifier value
   * @return new AreaId instance
   */
  public static AreaId of(Long value) {
    return new AreaId(value);
  }
}
