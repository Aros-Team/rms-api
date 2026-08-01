/* (C) 2026 */

package aros.services.rms.infraestructure.product.persistence.jpa;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Verifies that {@code associateOptionToProduct} persists {@code extra_price} and {@code
 * display_order}.
 */
@ExtendWith(MockitoExtension.class)
class ProductOptionRepositoryUpsertTest {

  @Mock private ProductOptionRepository productOptionRepository;
  @Mock private ProductMapper productMapper;

  private ProductOptionPersistenceAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new ProductOptionPersistenceAdapter(productOptionRepository, productMapper, null);
  }

  @Test
  void should_persist_extra_price_and_display_order_when_upserting_association() {
    adapter.upsertOptionAssociation(42L, 7L, new BigDecimal("2500.00"), 3);

    verify(productOptionRepository).upsertOptionAssociation(42L, 7L, new BigDecimal("2500.00"), 3);
    verifyNoMoreInteractions(productOptionRepository);
  }

  @Test
  void should_persist_null_extra_price_when_upserting_with_null() {
    adapter.upsertOptionAssociation(1L, 2L, null, 0);

    verify(productOptionRepository).upsertOptionAssociation(1L, 2L, null, 0);
  }

  @Test
  void should_call_associate_for_each_option_in_order() {
    adapter.associateOptionsToProduct(42L, List.of(1L, 2L, 3L));

    verify(productOptionRepository, times(3))
        .associateOptionToProduct(any(Long.class), any(Long.class));
    verify(productOptionRepository).associateOptionToProduct(42L, 1L);
    verify(productOptionRepository).associateOptionToProduct(42L, 2L);
    verify(productOptionRepository).associateOptionToProduct(42L, 3L);
  }

  @Test
  void should_remove_all_options_when_asked() {
    adapter.removeAllOptionsFromProduct(42L);

    verify(productOptionRepository).removeAllOptionsFromProduct(42L);
  }
}
