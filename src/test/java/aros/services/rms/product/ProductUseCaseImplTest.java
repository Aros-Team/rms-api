/* (C) 2026 */

package aros.services.rms.product;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.area.application.exception.AreaNotFoundException;
import aros.services.rms.core.area.port.output.AreaRepositoryPort;
import aros.services.rms.core.category.application.exception.CategoryNotFoundException;
import aros.services.rms.core.category.domain.Category;
import aros.services.rms.core.category.port.output.CategoryRepositoryPort;
import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.inventory.port.input.InventoryStockUseCase;
import aros.services.rms.core.inventory.port.output.ProductRecipeRepositoryPort;
import aros.services.rms.core.inventory.port.output.SupplyVariantRepositoryPort;
import aros.services.rms.core.product.application.exception.ProductNotFoundException;
import aros.services.rms.core.product.application.service.ProductService;
import aros.services.rms.core.product.domain.Product;
import aros.services.rms.core.product.port.output.ProductOptionRepositoryPort;
import aros.services.rms.core.product.port.output.ProductRepositoryPort;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ProductUseCaseImplTest {

  @Mock private ProductRepositoryPort productRepositoryPort;
  @Mock private AreaRepositoryPort areaRepositoryPort;
  @Mock private CategoryRepositoryPort categoryRepositoryPort;
  @Mock private Logger logger;
  @Mock private ProductRecipeRepositoryPort productRecipeRepositoryPort;
  @Mock private SupplyVariantRepositoryPort supplyVariantRepositoryPort;
  @Mock private InventoryStockUseCase inventoryStockUseCase;
  @Mock private ProductOptionRepositoryPort productOptionRepositoryPort;
  @Mock private ApplicationEventPublisher eventPublisher;

  private ProductService productUseCase;

  @BeforeEach
  void setUp() {
    productUseCase =
        new ProductService(
            productRepositoryPort,
            areaRepositoryPort,
            categoryRepositoryPort,
            productRecipeRepositoryPort,
            supplyVariantRepositoryPort,
            inventoryStockUseCase,
            productOptionRepositoryPort,
            eventPublisher,
            logger);
  }

  @Test
  void shouldCreateProductSuccessfully() {
    Product product =
        Product.builder()
            .name("Burger")
            .basePrice(new Money(BigDecimal.valueOf(10.0), Currency.getInstance("COP")))
            .category(Category.builder().id(1L).build())
            .preparationAreaId(1L)
            .build();
    Product saved =
        Product.builder()
            .id(1L)
            .name("Burger")
            .basePrice(new Money(BigDecimal.valueOf(10.0), Currency.getInstance("COP")))
            .active(true)
            .category(Category.builder().id(1L).name("Food").build())
            .preparationAreaId(1L)
            .build();

    when(areaRepositoryPort.existsById(1L)).thenReturn(true);
    when(categoryRepositoryPort.existsById(1L)).thenReturn(true);
    when(productRepositoryPort.save(any(Product.class))).thenReturn(saved);

    Product result = productUseCase.create(product);

    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals("Burger", result.getName());
  }

  @Test
  void shouldThrowWhenAreaNotFoundOnCreate() {
    Product product =
        Product.builder()
            .name("Burger")
            .basePrice(new Money(BigDecimal.valueOf(10.0), Currency.getInstance("COP")))
            .category(Category.builder().id(1L).build())
            .preparationAreaId(99L)
            .build();

    when(areaRepositoryPort.existsById(99L)).thenReturn(false);

    assertThrows(AreaNotFoundException.class, () -> productUseCase.create(product));
  }

  @Test
  void shouldThrowWhenCategoryNotFoundOnCreate() {
    Product product =
        Product.builder()
            .name("Burger")
            .basePrice(new Money(BigDecimal.valueOf(10.0), Currency.getInstance("COP")))
            .category(Category.builder().id(99L).build())
            .preparationAreaId(1L)
            .build();

    when(areaRepositoryPort.existsById(1L)).thenReturn(true);
    when(categoryRepositoryPort.existsById(99L)).thenReturn(false);

    assertThrows(CategoryNotFoundException.class, () -> productUseCase.create(product));
  }

  @Test
  void shouldDisableProductSuccessfully() {
    Product existing =
        Product.builder()
            .id(1L)
            .name("Burger")
            .active(true)
            .basePrice(new Money(BigDecimal.valueOf(10.0), Currency.getInstance("COP")))
            .build();
    Product saved =
        Product.builder()
            .id(1L)
            .name("Burger")
            .active(false)
            .basePrice(new Money(BigDecimal.valueOf(10.0), Currency.getInstance("COP")))
            .build();

    when(productRepositoryPort.findById(1L)).thenReturn(Optional.of(existing));
    when(productRepositoryPort.save(any(Product.class))).thenReturn(saved);

    Product result = productUseCase.disable(1L);

    assertFalse(result.isActive());
  }

  @Test
  void shouldThrowWhenDisablingNonExistentProduct() {
    when(productRepositoryPort.findById(99L)).thenReturn(Optional.empty());

    assertThrows(ProductNotFoundException.class, () -> productUseCase.disable(99L));
  }

  @Test
  void shouldFindAllProducts() {
    when(productRepositoryPort.findAll())
        .thenReturn(
            List.of(
                Product.builder().id(1L).name("Burger").build(),
                Product.builder().id(2L).name("Water").build()));

    List<Product> result = productUseCase.findAll();

    assertEquals(2, result.size());
  }

  @Test
  void shouldFindAllActiveWithSelections() {
    Pageable pageable = PageRequest.of(0, 20);
    List<Product> products =
        List.of(
            Product.builder()
                .id(1L)
                .name("Burger")
                .selectionType(
                    aros.services.rms.core.specialselection.domain.SelectionType.STANDARD)
                .build(),
            Product.builder()
                .id(2L)
                .name("Special")
                .selectionType(
                    aros.services.rms.core.specialselection.domain.SelectionType.SPECIAL_SELECTION)
                .build());
    PageImpl<Product> page = new PageImpl<>(products, pageable, 2);

    when(productRepositoryPort.findAllActive(pageable)).thenReturn(page);

    var result = productUseCase.findAllActive(pageable, true);

    assertEquals(2, result.getTotalElements());
    verify(productRepositoryPort).findAllActive(pageable);
  }

  @Test
  void shouldFindAllActiveWithoutSelections() {
    Pageable pageable = PageRequest.of(0, 20);
    List<Product> products =
        List.of(
            Product.builder()
                .id(1L)
                .name("Burger")
                .selectionType(
                    aros.services.rms.core.specialselection.domain.SelectionType.STANDARD)
                .build());
    PageImpl<Product> page = new PageImpl<>(products, pageable, 1);

    when(productRepositoryPort.findAllStandard(pageable)).thenReturn(page);

    var result = productUseCase.findAllActive(pageable, false);

    assertEquals(1, result.getTotalElements());
    verify(productRepositoryPort).findAllStandard(pageable);
  }
}
