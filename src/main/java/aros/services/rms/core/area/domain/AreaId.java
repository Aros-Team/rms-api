/* (C) 2026 */
package aros.services.rms.core.area.domain;

public record AreaId(Long value) {
  public AreaId {
    if (value == null) {
      throw new IllegalArgumentException("El ID del area no puede ser nulo");
    }
  }

  public static AreaId of(Long value) {
    return new AreaId(value);
  }
}
