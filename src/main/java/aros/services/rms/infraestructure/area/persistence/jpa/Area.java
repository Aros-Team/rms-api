/* (C) 2026 */

package aros.services.rms.infraestructure.area.persistence.jpa;

import aros.services.rms.core.area.domain.AreaActivity;
import aros.services.rms.infraestructure.order.persistence.Order;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/** JPA entity representing a preparation area in the database. */
@Entity
@jakarta.persistence.Table(name = "areas")
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Area {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "area_activities", joinColumns = @JoinColumn(name = "area_id"))
  @Column(name = "activity", nullable = false)
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private Set<AreaActivity> activities = new HashSet<>();

  @Column(nullable = false)
  @Builder.Default
  private boolean enabled = true;

  @Column(nullable = false)
  private boolean forUser;

  @Column(nullable = false)
  private boolean forProduct;

  @Column(nullable = false)
  @Builder.Default
  private boolean exclusive = false;

  @ManyToMany(mappedBy = "preparationAreas")
  @Builder.Default
  @ToString.Exclude
  private Set<Order> orders = new HashSet<>();

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Area area = (Area) o;
    return id != null && Objects.equals(id, area.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
