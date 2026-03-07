package aros.services.rms.infraestructure.order.persistence;

import aros.services.rms.infraestructure.product.persistence.Product;
import aros.services.rms.infraestructure.product.persistence.ProductOption;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_details")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private Double unitPrice;

    private String instructions;

    @ManyToMany
    @JoinTable(
            name = "order_detail_options",
            joinColumns = @JoinColumn(name = "order_detail_id"),
            inverseJoinColumns = @JoinColumn(name = "option_id"))
    private List<ProductOption> selectedOptions;
}