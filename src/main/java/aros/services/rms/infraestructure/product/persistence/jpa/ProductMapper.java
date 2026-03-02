package aros.services.rms.infraestructure.product.persistence.jpa;

import aros.services.rms.core.product.domain.Product;
import aros.services.rms.core.product.domain.ProductOption;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toProductDomain(aros.services.rms.infraestructure.product.persistence.Product entity) {
        if (entity == null) return null;
        return Product.builder()
                .id(entity.getId())
                .name(entity.getName())
                .basePrice(entity.getBasePrice())
                .category(entity.getCategory())
                .build();
    }

    public ProductOption toProductOptionDomain(aros.services.rms.infraestructure.product.persistence.ProductOption entity) {
        if (entity == null) return null;
        return ProductOption.builder()
                .id(entity.getId())
                .name(entity.getName())
                .type(entity.getType())
                .build();
    }

    public aros.services.rms.infraestructure.product.persistence.Product toProductEntity(Product domain) {
        if (domain == null) return null;
        return aros.services.rms.infraestructure.product.persistence.Product.builder()
                .id(domain.getId())
                .name(domain.getName())
                .basePrice(domain.getBasePrice())
                .category(domain.getCategory())
                .build();
    }

    public aros.services.rms.infraestructure.product.persistence.ProductOption toProductOptionEntity(ProductOption domain) {
        if (domain == null) return null;
        return aros.services.rms.infraestructure.product.persistence.ProductOption.builder()
                .id(domain.getId())
                .name(domain.getName())
                .type(domain.getType())
                .build();
    }
}