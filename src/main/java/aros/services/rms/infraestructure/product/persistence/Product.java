/* (C) 2026 */
package aros.services.rms.infraestructure.product.persistence;

import aros.services.rms.infraestructure.area.persistence.jpa.Area;
import aros.services.rms.infraestructure.category.persistence.Category;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** JPA entity representing a product in the database. */
@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  private Double basePrice;

  @Column(name = "has_options", nullable = false)
  private boolean hasOptions;

  @Column(nullable = false)
  @Builder.Default
  private boolean active = true;

  @ManyToOne
  @JoinColumn(name = "category_id")
  private Category category;

  @ManyToOne
  @JoinColumn(name = "area_id")
  private Area preparationArea;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "product_product_options",
      joinColumns = @JoinColumn(name = "product_id"),
      inverseJoinColumns = @JoinColumn(name = "option_id"))
  @Builder.Default
  private List<ProductOption> options = new ArrayList<>();
}
