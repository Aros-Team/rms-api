/* (C) 2026 */

package aros.services.rms.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.category.domain.OptionGroup;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.order.domain.ClarificationAnswer;
import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderDetail;
import aros.services.rms.core.order.domain.OrderStatus;
import aros.services.rms.core.product.domain.Product;
import aros.services.rms.core.product.domain.ProductOption;
import aros.services.rms.core.product.port.output.ProductRepositoryPort;
import aros.services.rms.core.specialselection.domain.SpecialSelectionAddition;
import aros.services.rms.core.specialselection.domain.SpecialSelectionConfiguration;
import aros.services.rms.core.specialselection.domain.SpecialSelectionQuestion;
import aros.services.rms.core.specialselection.port.output.SpecialSelectionRepositoryPort;
import aros.services.rms.core.table.domain.Table;
import aros.services.rms.core.table.domain.TableStatus;
import aros.services.rms.infraestructure.order.api.dto.OrderResponse;
import aros.services.rms.infraestructure.order.api.dto.OrderResponseMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderResponseMapperTest {

  @Mock private ProductRepositoryPort productRepositoryPort;
  @Mock private SpecialSelectionRepositoryPort specialSelectionRepositoryPort;

  private OrderResponseMapper orderResponseMapper;

  @BeforeEach
  void setUp() {
    orderResponseMapper =
        new OrderResponseMapper(productRepositoryPort, specialSelectionRepositoryPort);
  }

  @Test
  void should_returnNull_when_orderIsNull() {
    assertNull(orderResponseMapper.toResponse(null));
  }

  @Test
  void should_resolveSelectedProductsAndAdditions_when_comboOrder() {
    // Arrange
    Product burger =
        Product.builder()
            .id(10L)
            .name("Burger")
            .basePrice(new Money(BigDecimal.valueOf(12.5), Currency.getInstance("COP")))
            .build();
    Product fries =
        Product.builder()
            .id(11L)
            .name("Fries")
            .basePrice(new Money(BigDecimal.valueOf(5.0), Currency.getInstance("COP")))
            .build();
    Product drink =
        Product.builder()
            .id(12L)
            .name("Drink")
            .basePrice(new Money(BigDecimal.valueOf(3.0), Currency.getInstance("COP")))
            .build();

    SpecialSelectionAddition addition =
        SpecialSelectionAddition.builder().id(5L).name("Extra Cheese").extraPrice(2.0).build();
    SpecialSelectionQuestion question =
        SpecialSelectionQuestion.builder().id(1L).question("How cooked?").build();
    SpecialSelectionConfiguration config =
        SpecialSelectionConfiguration.builder()
            .productId(10L)
            .additions(List.of(addition))
            .questions(List.of(question))
            .build();

    OptionGroup cat = OptionGroup.builder().id(1L).name("Main").build();
    ProductOption option = ProductOption.builder().id(1L).name("Large").category(cat).build();

    OrderDetail detail =
        OrderDetail.builder()
            .id(1L)
            .product(burger)
            .unitPrice(new Money(BigDecimal.valueOf(12.5), Currency.getInstance("COP")))
            .instructions("No onions")
            .selectedOptions(List.of(option))
            .selectedProductIds(List.of(10L, 11L))
            .additionIds(List.of(5L))
            .clarifications(List.of(new ClarificationAnswer(1L, "Medium", null)))
            .build();

    Table table = Table.builder().id(1L).status(TableStatus.OCCUPIED).build();
    Order order =
        Order.builder()
            .id(42L)
            .date(LocalDateTime.of(2026, 3, 8, 14, 30))
            .status(OrderStatus.QUEUE)
            .table(table)
            .details(List.of(detail))
            .build();

    when(productRepositoryPort.findAllById(List.of(10L, 11L))).thenReturn(List.of(burger, fries));
    when(specialSelectionRepositoryPort.findById(10L)).thenReturn(Optional.of(config));

    // Act
    OrderResponse response = orderResponseMapper.toResponse(order);

    // Assert
    assertNotNull(response);
    assertEquals(42L, response.id());
    assertEquals("QUEUE", response.status());
    assertEquals(1L, response.tableId());
    assertEquals(1, response.details().size());

    OrderResponse.OrderDetailResponse dr = response.details().get(0);
    assertEquals(10L, dr.productId());
    assertEquals("Burger", dr.productName());
    assertEquals(12.5, dr.unitPrice());
    assertEquals("No onions", dr.instructions());

    // Selected products resolved
    assertEquals(2, dr.selectedProductIds().size());
    assertEquals(2, dr.selectedProducts().size());
    assertEquals("Burger", dr.selectedProducts().get(0).name());
    assertEquals(12.5, dr.selectedProducts().get(0).basePrice());
    assertEquals("Fries", dr.selectedProducts().get(1).name());
    assertEquals(5.0, dr.selectedProducts().get(1).basePrice());

    // Additions resolved
    assertEquals(1, dr.additionIds().size());
    assertEquals(1, dr.selectedAdditions().size());
    assertEquals("Extra Cheese", dr.selectedAdditions().get(0).name());
    assertEquals(2.0, dr.selectedAdditions().get(0).extraPrice());

    // Clarifications resolved
    assertEquals(1, dr.clarifications().size());
    assertEquals(1L, dr.clarifications().get(0).questionId());
    assertEquals("How cooked?", dr.clarifications().get(0).question());
    assertEquals("Medium", dr.clarifications().get(0).answer());
  }

  @Test
  void should_returnEmptyLists_when_noComboData() {
    // Arrange
    Product product =
        Product.builder()
            .id(1L)
            .name("Pizza")
            .basePrice(new Money(BigDecimal.valueOf(15.0), Currency.getInstance("COP")))
            .build();
    OrderDetail detail =
        OrderDetail.builder()
            .id(1L)
            .product(product)
            .unitPrice(new Money(BigDecimal.valueOf(15.0), Currency.getInstance("COP")))
            .instructions(null)
            .selectedOptions(null)
            .selectedProductIds(null)
            .additionIds(null)
            .clarifications(null)
            .build();

    Order order =
        Order.builder()
            .id(1L)
            .date(LocalDateTime.now())
            .status(OrderStatus.QUEUE)
            .details(List.of(detail))
            .build();

    when(specialSelectionRepositoryPort.findById(1L)).thenReturn(Optional.empty());

    // Act
    OrderResponse response = orderResponseMapper.toResponse(order);

    // Assert
    assertNotNull(response);
    OrderResponse.OrderDetailResponse dr = response.details().get(0);
    assertTrue(dr.selectedProducts().isEmpty());
    assertTrue(dr.selectedAdditions().isEmpty());
    assertTrue(dr.clarifications().isEmpty());

    verify(productRepositoryPort, never()).findAllById(anyList());
  }

  @Test
  void should_handleMissingProductsGracefully_when_productNotFound() {
    // Arrange
    Product detailProduct =
        Product.builder()
            .id(1L)
            .name("Combo")
            .basePrice(new Money(BigDecimal.valueOf(20.0), Currency.getInstance("COP")))
            .build();

    OrderDetail detail =
        OrderDetail.builder()
            .id(1L)
            .product(detailProduct)
            .unitPrice(new Money(BigDecimal.valueOf(20.0), Currency.getInstance("COP")))
            .selectedProductIds(List.of(99L))
            .build();

    Order order =
        Order.builder()
            .id(1L)
            .date(LocalDateTime.now())
            .status(OrderStatus.QUEUE)
            .details(List.of(detail))
            .build();

    when(productRepositoryPort.findAllById(List.of(99L))).thenReturn(List.of());
    when(specialSelectionRepositoryPort.findById(1L)).thenReturn(Optional.empty());

    // Act
    OrderResponse response = orderResponseMapper.toResponse(order);

    // Assert
    assertNotNull(response);
    OrderResponse.OrderDetailResponse dr = response.details().get(0);
    assertEquals(1, dr.selectedProducts().size());
    assertNull(dr.selectedProducts().get(0).name());
    assertNull(dr.selectedProducts().get(0).basePrice());
  }
}
