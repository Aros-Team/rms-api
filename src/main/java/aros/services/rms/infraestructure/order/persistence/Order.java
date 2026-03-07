/* (C) 2026 */
package aros.services.rms.infraestructure.order.persistence;

import aros.services.rms.infraestructure.area.persistence.jpa.Area;
import aros.services.rms.infraestructure.table.persistence.Table;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@jakarta.persistence.Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Builder.Default private LocalDateTime date = LocalDateTime.now();

  @Enumerated(EnumType.STRING)
  @Builder.Default
  private OrderStatus status = OrderStatus.QUEUE;

  @ManyToOne
  @JoinColumn(name = "table_id")
  private Table table;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<OrderDetail> details = new ArrayList<>();

  @ManyToMany
  @JoinTable(
      name = "order_preparation_areas",
      joinColumns = @JoinColumn(name = "order_id"),
      inverseJoinColumns = @JoinColumn(name = "area_id"))
  @Builder.Default
  private Set<Area> preparationAreas = new HashSet<>();
}
