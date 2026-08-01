/* (C) 2026 */

package aros.services.rms.infraestructure.order.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import aros.services.rms.core.category.domain.OptionCategory;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderDetail;
import aros.services.rms.core.product.domain.Product;
import aros.services.rms.core.product.domain.ProductOption;
import aros.services.rms.infraestructure.order.persistence.OrderDetailOption;
import aros.services.rms.infraestructure.order.persistence.OrderDetailOption.OrderDetailOptionId;
import aros.services.rms.infraestructure.product.persistence.jpa.ProductMapper;
import aros.services.rms.infraestructure.table.persistence.jpa.TableMapper;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit test for the Phase C changes in {@link OrderMapper}: round-tripping an {@link
 * aros.services.rms.infraestructure.order.persistence.OrderDetail} that has {@link
 * OrderDetailOption} rows with non-zero {@code extra_price} (V38 schema addition).
 *
 * <p>Verifies that the per-row surcharge ({@code extra_price}) is preserved both directions and
 * that the {@code extraCharge} field on the domain is computed as the sum of those values.
 */
@ExtendWith(MockitoExtension.class)
class OrderMapperOptionPersistenceTest {

  @Mock private TableMapper tableMapper;
  @Mock private ProductMapper productMapper;

  private OrderMapper orderMapper;

  private static final Long ORDER_DETAIL_ID = 100L;
  private static final Long OPTION_ID = 42L;

  @BeforeEach
  void setUp() {
    orderMapper = new OrderMapper(tableMapper, productMapper);
  }

  @Test
  void toDomain_sumsJoinRowExtraPrice_intoExtraCharge_and_rebuildsSelectedOptions() {
    aros.services.rms.infraestructure.product.persistence.Product productEntity =
        aros.services.rms.infraestructure.product.persistence.Product.builder()
            .id(1L)
            .name("Burger")
            .basePrice(10.0)
            .build();
    aros.services.rms.infraestructure.product.persistence.ProductOption optionEntity =
        aros.services.rms.infraestructure.product.persistence.ProductOption.builder()
            .id(OPTION_ID)
            .name("Extra Cheese")
            .build();
    aros.services.rms.infraestructure.order.persistence.Order orderEntity =
        aros.services.rms.infraestructure.order.persistence.Order.builder()
            .id(7L)
            .status(aros.services.rms.infraestructure.order.persistence.OrderStatus.QUEUE)
            .build();
    OrderDetailOption row =
        OrderDetailOption.builder()
            .id(new OrderDetailOptionId(ORDER_DETAIL_ID, OPTION_ID))
            .orderDetail(
                aros.services.rms.infraestructure.order.persistence.OrderDetail.builder()
                    .id(ORDER_DETAIL_ID)
                    .order(orderEntity)
                    .build())
            .option(optionEntity)
            .extraPrice(2.5)
            .build();
    aros.services.rms.infraestructure.order.persistence.OrderDetail detailEntity =
        aros.services.rms.infraestructure.order.persistence.OrderDetail.builder()
            .id(ORDER_DETAIL_ID)
            .order(orderEntity)
            .product(productEntity)
            .unitPrice(12.5)
            .instructions(null)
            .selectedOptions(List.of(row))
            .build();
    orderEntity.setDetails(List.of(detailEntity));

    Product productDomain =
        Product.builder()
            .id(1L)
            .name("Burger")
            .basePrice(new Money(BigDecimal.valueOf(10.0), Currency.getInstance("COP")))
            .build();
    ProductOption optionDomain =
        ProductOption.builder()
            .id(OPTION_ID)
            .name("Extra Cheese")
            .category(optionCategory())
            .build();

    when(productMapper.toProductDomain(productEntity)).thenReturn(productDomain);
    when(productMapper.toProductOptionDomain(optionEntity)).thenReturn(optionDomain);

    Order domain = orderMapper.toDomain(orderEntity);

    assertNotNull(domain);
    assertEquals(1, domain.getDetails().size());
    OrderDetail detail = domain.getDetails().get(0);
    assertEquals(12.5, detail.getUnitPrice().amount().doubleValue(), 0.001);
    // extraCharge = sum of join rows' extra_price = 2.5
    assertEquals(2.5, detail.getExtraCharge().amount().doubleValue(), 0.001);
    assertEquals(1, detail.getSelectedOptions().size());
    assertEquals(OPTION_ID, detail.getSelectedOptions().get(0).getId());
    // optionExtraPrices carries the same surcharge keyed by option id
    assertEquals(
        new Money(BigDecimal.valueOf(2.5), Currency.getInstance("COP")),
        detail.getOptionExtraPrices().get(OPTION_ID));
  }

  @Test
  void roundTrip_preservesExtraPrice_perJoinRow() {
    aros.services.rms.infraestructure.product.persistence.Product productEntity =
        aros.services.rms.infraestructure.product.persistence.Product.builder()
            .id(1L)
            .name("Burger")
            .basePrice(10.0)
            .build();
    aros.services.rms.infraestructure.product.persistence.ProductOption optionEntity =
        aros.services.rms.infraestructure.product.persistence.ProductOption.builder()
            .id(OPTION_ID)
            .name("Extra Cheese")
            .build();
    aros.services.rms.infraestructure.order.persistence.Order orderEntity =
        aros.services.rms.infraestructure.order.persistence.Order.builder()
            .id(7L)
            .status(aros.services.rms.infraestructure.order.persistence.OrderStatus.QUEUE)
            .build();
    OrderDetailOption row =
        OrderDetailOption.builder()
            .id(new OrderDetailOptionId(ORDER_DETAIL_ID, OPTION_ID))
            .orderDetail(
                aros.services.rms.infraestructure.order.persistence.OrderDetail.builder()
                    .id(ORDER_DETAIL_ID)
                    .order(orderEntity)
                    .build())
            .option(optionEntity)
            .extraPrice(7.25)
            .build();
    aros.services.rms.infraestructure.order.persistence.OrderDetail detailEntity =
        aros.services.rms.infraestructure.order.persistence.OrderDetail.builder()
            .id(ORDER_DETAIL_ID)
            .order(orderEntity)
            .product(productEntity)
            .unitPrice(17.25)
            .selectedOptions(List.of(row))
            .build();
    orderEntity.setDetails(List.of(detailEntity));

    Product productDomain =
        Product.builder()
            .id(1L)
            .name("Burger")
            .basePrice(new Money(BigDecimal.valueOf(10.0), Currency.getInstance("COP")))
            .build();
    ProductOption optionDomain =
        ProductOption.builder()
            .id(OPTION_ID)
            .name("Extra Cheese")
            .category(optionCategory())
            .build();

    when(productMapper.toProductDomain(productEntity)).thenReturn(productDomain);
    when(productMapper.toProductOptionEntity(optionDomain)).thenReturn(optionEntity);
    when(productMapper.toProductOptionDomain(optionEntity)).thenReturn(optionDomain);

    Order domain = orderMapper.toDomain(orderEntity);

    // Round-trip back to entity.
    aros.services.rms.infraestructure.order.persistence.Order roundTrippedEntity =
        orderMapper.toEntity(domain);

    assertEquals(1, roundTrippedEntity.getDetails().size());
    aros.services.rms.infraestructure.order.persistence.OrderDetail roundTrippedDetail =
        roundTrippedEntity.getDetails().get(0);
    assertEquals(1, roundTrippedDetail.getSelectedOptions().size());
    OrderDetailOption roundTrippedRow = roundTrippedDetail.getSelectedOptions().get(0);
    assertEquals(7.25, roundTrippedRow.getExtraPrice(), 0.001);
    assertEquals(OPTION_ID, roundTrippedRow.getOption().getId());
  }

  private static OptionCategory optionCategory() {
    return OptionCategory.builder().id(50L).name("Adición").build();
  }
}
