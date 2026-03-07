/* (C) 2026 */
package aros.services.rms.infraestructure.area.persistence.jpa;

import aros.services.rms.infraestructure.area.persistence.AreaType;
import aros.services.rms.infraestructure.order.persistence.Order;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@jakarta.persistence.Table(name = "areas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Area {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  @Enumerated(EnumType.STRING)
  private AreaType type;

  @ManyToMany(mappedBy = "preparationAreas")
  @Builder.Default
  private Set<Order> orders = new HashSet<>();
}
