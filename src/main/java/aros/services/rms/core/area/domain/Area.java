/* (C) 2026 */
package aros.services.rms.core.area.domain;

public class Area {
  private AreaId id;
  private String name;

  public Area(AreaId id, String name) {
    this.id = id;
    this.name = name;
  }

  public AreaId getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void rename(String name) {
    this.name = name;
  }
}
