package aros.services.rms.core.product.port.output;

import aros.services.rms.core.product.domain.ProductOption;
import java.util.List;

public interface ProductOptionRepositoryPort {
    List<ProductOption> findAllById(List<Long> ids);
}