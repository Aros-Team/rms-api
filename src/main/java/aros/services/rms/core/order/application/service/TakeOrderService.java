/* (C) 2026 */

package aros.services.rms.core.order.application.service;

import aros.services.rms.core.category.domain.OptionSelectionType;
import aros.services.rms.core.common.metrics.BusinessMetricsPort;
import aros.services.rms.core.common.money.domain.Money;
import aros.services.rms.core.inventory.application.exception.InsufficientStockException;
import aros.services.rms.core.inventory.port.input.InventoryMovementUseCase;
import aros.services.rms.core.inventory.port.input.InventoryStockUseCase;
import aros.services.rms.core.order.application.dto.TakeOrderCommand;
import aros.services.rms.core.order.application.exception.SingleChoiceCategoryLimitException;
import aros.services.rms.core.order.domain.Order;
import aros.services.rms.core.order.domain.OrderDetail;
import aros.services.rms.core.order.port.input.TakeOrderUseCase;
import aros.services.rms.core.order.port.output.OrderRepositoryPort;
import aros.services.rms.core.product.application.exception.InvalidProductOptionException;
import aros.services.rms.core.product.domain.Product;
import aros.services.rms.core.product.domain.ProductOption;
import aros.services.rms.core.product.domain.ProductOptionCostProfile;
import aros.services.rms.core.product.port.output.ProductOptionRepositoryPort;
import aros.services.rms.core.product.port.output.ProductRepositoryPort;
import aros.services.rms.core.specialselection.application.exception.SpecialSelectionNotAvailableException;
import aros.services.rms.core.specialselection.application.service.SpecialSelectionAvailabilityService;
import aros.services.rms.core.specialselection.application.service.SpecialSelectionPricingService;
import aros.services.rms.core.specialselection.application.service.SpecialSelectionValidator;
import aros.services.rms.core.specialselection.domain.SelectionType;
import aros.services.rms.core.specialselection.domain.SpecialSelectionConfiguration;
import aros.services.rms.core.specialselection.port.output.SpecialSelectionRepositoryPort;
import aros.services.rms.core.table.domain.Table;
import aros.services.rms.core.table.domain.TableStatus;
import aros.services.rms.core.table.port.output.TableRepositoryPort;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the use case to take a new order, occupy the table, validate product options
 * and special selections, and deduct inventory.
 *
 * <p>Phase C — orders: enforces SINGLE_CHOICE max-1 selection semantics and computes the final
 * {@code unitPrice} as {@code basePrice + Σ extra_price} for the selected {@code
 * OptionSelectionType#EXTRA} options. The resulting surcharge is recorded on {@link
 * OrderDetail#getExtraCharge()} and exposed downstream.
 */
public class TakeOrderService implements TakeOrderUseCase {

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(TakeOrderService.class);
  private final OrderRepositoryPort orderRepositoryPort;
  private final TableRepositoryPort tableRepositoryPort;
  private final ProductRepositoryPort productRepositoryPort;
  private final ProductOptionRepositoryPort productOptionRepositoryPort;
  private final InventoryStockUseCase inventoryStockUseCase;
  private final InventoryMovementUseCase inventoryMovementUseCase;
  private final BusinessMetricsPort metricsPort;
  private final SpecialSelectionRepositoryPort specialSelectionRepositoryPort;
  private final SpecialSelectionValidator specialSelectionValidator;
  private final SpecialSelectionPricingService specialSelectionPricingService;
  private final SpecialSelectionAvailabilityService specialSelectionAvailabilityService;

  /**
   * Creates a new take order service.
   *
   * @param orderRepositoryPort the order repository port
   * @param tableRepositoryPort the table repository port
   * @param productRepositoryPort the product repository port
   * @param productOptionRepositoryPort the product option repository port
   * @param inventoryStockUseCase the inventory stock use case
   * @param inventoryMovementUseCase the inventory movement use case
   * @param metricsPort the business metrics port
   * @param specialSelectionRepositoryPort the special selection repository port
   * @param specialSelectionValidator the special selection validator
   * @param specialSelectionPricingService the special selection pricing service
   * @param specialSelectionAvailabilityService the special selection availability service
   */
  public TakeOrderService(
      OrderRepositoryPort orderRepositoryPort,
      TableRepositoryPort tableRepositoryPort,
      ProductRepositoryPort productRepositoryPort,
      ProductOptionRepositoryPort productOptionRepositoryPort,
      InventoryStockUseCase inventoryStockUseCase,
      InventoryMovementUseCase inventoryMovementUseCase,
      BusinessMetricsPort metricsPort,
      SpecialSelectionRepositoryPort specialSelectionRepositoryPort,
      SpecialSelectionValidator specialSelectionValidator,
      SpecialSelectionPricingService specialSelectionPricingService,
      SpecialSelectionAvailabilityService specialSelectionAvailabilityService) {
    this.orderRepositoryPort = orderRepositoryPort;
    this.tableRepositoryPort = tableRepositoryPort;
    this.productRepositoryPort = productRepositoryPort;
    this.productOptionRepositoryPort = productOptionRepositoryPort;
    this.inventoryStockUseCase = inventoryStockUseCase;
    this.inventoryMovementUseCase = inventoryMovementUseCase;
    this.metricsPort = metricsPort;
    this.specialSelectionRepositoryPort = specialSelectionRepositoryPort;
    this.specialSelectionValidator = specialSelectionValidator;
    this.specialSelectionPricingService = specialSelectionPricingService;
    this.specialSelectionAvailabilityService = specialSelectionAvailabilityService;
  }

  @Override
  public Order execute(TakeOrderCommand command) {
    log.debug("TakeOrderService.execute called for table {}", command.getTableId());
    Table table =
        tableRepositoryPort
            .findById(command.getTableId())
            .orElseThrow(() -> new IllegalArgumentException("Table not found"));

    if (table.getStatus() != TableStatus.AVAILABLE) {
      throw new IllegalStateException("Table is not available");
    }

    table.setStatus(TableStatus.OCCUPIED);
    tableRepositoryPort.save(table);

    try {
      Order order =
          Order.builder().table(table).date(LocalDateTime.now()).details(new ArrayList<>()).build();

      for (TakeOrderCommand.OrderDetailCommand detailCommand : command.getDetails()) {
        Product product =
            productRepositoryPort
                .findById(detailCommand.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        boolean hasSelectedOptions =
            detailCommand.getSelectedOptionIds() != null
                && !detailCommand.getSelectedOptionIds().isEmpty();

        List<ProductOption> selectedOptions = new ArrayList<>();
        if (hasSelectedOptions) {
          selectedOptions =
              productOptionRepositoryPort.findAllById(detailCommand.getSelectedOptionIds());

          for (ProductOption option : selectedOptions) {
            if (!productOptionRepositoryPort.isOptionAssociatedWithProduct(
                product.getId(), option.getId())) {
              throw new InvalidProductOptionException(product.getId(), option.getId());
            }
          }

          // Phase C: enforce SINGLE_CHOICE max-1 per category.
          enforceSingleChoiceLimit(selectedOptions);
        }

        Money unitPrice = product.getBasePrice();
        Money extraCharge = Money.zero(unitPrice.currency());
        Map<Long, Money> optionExtraPrices = new HashMap<>();

        if (product.getSelectionType() == SelectionType.SPECIAL_SELECTION) {
          Optional<SpecialSelectionConfiguration> configOpt =
              specialSelectionRepositoryPort.findById(product.getId());
          if (configOpt.isPresent()) {
            SpecialSelectionConfiguration config = configOpt.get();
            if (!specialSelectionAvailabilityService.isAvailable(
                config, java.time.LocalDateTime.now())) {
              throw new SpecialSelectionNotAvailableException(product.getId());
            }
            specialSelectionValidator.validateOrderSelections(
                config,
                detailCommand.getSelectedProductIds(),
                detailCommand.getAdditionIds(),
                detailCommand.getClarifications());
            Double computedPrice =
                specialSelectionPricingService.computeUnitPrice(
                    config, detailCommand.getAdditionIds());
            unitPrice = new Money(BigDecimal.valueOf(computedPrice), Currency.getInstance("COP"));
          }
        } else if (hasSelectedOptions) {
          // Phase C: STANDARD products charge extra_price of EXTRA selections on top of the
          // base price. SINGLE_CHOICE / MULTI_SELECT / REMOVE selections do not contribute to
          // unitPrice through this path.
          Map<Long, Money> extraPriceByOptionId =
              loadExtraPricesByOptionId(product.getId(), selectedOptions);
          for (ProductOption option : selectedOptions) {
            Money surcharge =
                extraPriceByOptionId.getOrDefault(option.getId(), Money.zero(unitPrice.currency()));
            optionExtraPrices.put(option.getId(), surcharge);
            if (isExtraCategory(option)) {
              extraCharge = extraCharge.plus(surcharge);
            }
          }
          unitPrice = unitPrice.plus(extraCharge);
        }

        OrderDetail detail =
            OrderDetail.builder()
                .product(product)
                .unitPrice(unitPrice)
                .extraCharge(extraCharge)
                .optionExtraPrices(optionExtraPrices)
                .instructions(detailCommand.getInstructions())
                .selectedOptions(selectedOptions)
                .selectedProductIds(detailCommand.getSelectedProductIds())
                .additionIds(detailCommand.getAdditionIds())
                .clarifications(detailCommand.getClarifications())
                .build();

        order.getDetails().add(detail);
      }

      for (OrderDetail detail : order.getDetails()) {
        List<Long> selectedOptionIds =
            detail.getSelectedOptions() != null
                ? detail.getSelectedOptions().stream().map(ProductOption::getId).toList()
                : List.of();

        if (!inventoryStockUseCase.isAvailable(detail.getProduct().getId(), selectedOptionIds)) {
          metricsPort.recordInsufficientStock();
          throw new InsufficientStockException(
              detail.getProduct().getId(),
              aros.services.rms.core.inventory.domain.MovementType.DEDUCTION,
              "Insufficient stock for product: " + detail.getProduct().getName());
        }
      }

      Order savedOrder = orderRepositoryPort.save(order);

      try {
        inventoryMovementUseCase.deductForOrder(savedOrder.getId(), savedOrder.getDetails());
        metricsPort.recordInventoryDeduction(true);
      } catch (InsufficientStockException e) {
        metricsPort.recordInventoryDeduction(false);
        throw e;
      }
      metricsPort.recordOrderCreated(true);
      log.info("METRICS: recordOrderCreated(true) called for table {}", command.getTableId());

      return savedOrder;
    } catch (Exception e) {
      table.setStatus(TableStatus.AVAILABLE);
      tableRepositoryPort.save(table);
      metricsPort.recordOrderCreated(false);
      log.warn(
          "METRICS: recordOrderCreated(false) called for table {}, reason: {}",
          command.getTableId(),
          e.getMessage());
      throw e;
    }
  }

  /**
   * Enforces the SINGLE_CHOICE max-1 selection rule for an order detail. Group the options by their
   * OptionCategory and, for any category whose selectionType is {@code SINGLE_CHOICE}, throw {@link
   * SingleChoiceCategoryLimitException} when more than one option was selected.
   */
  private void enforceSingleChoiceLimit(List<ProductOption> selectedOptions) {
    Map<Long, List<ProductOption>> byCategoryId =
        selectedOptions.stream()
            .filter(opt -> opt.getCategory() != null && opt.getCategory().getId() != null)
            .collect(Collectors.groupingBy(opt -> opt.getCategory().getId()));
    for (Map.Entry<Long, List<ProductOption>> entry : byCategoryId.entrySet()) {
      List<ProductOption> opts = entry.getValue();
      if (opts.size() <= 1) {
        continue;
      }
      ProductOption sample = opts.get(0);
      if (sample.getCategory().getSelectionType() == OptionSelectionType.SINGLE_CHOICE) {
        throw new SingleChoiceCategoryLimitException(
            sample.getCategory().getId(), sample.getCategory().getName(), opts.size());
      }
    }
  }

  private static boolean isExtraCategory(ProductOption option) {
    return option != null
        && option.getCategory() != null
        && option.getCategory().getSelectionType() == OptionSelectionType.EXTRA;
  }

  /**
   * Loads the per-option surcharge ({@code extra_price}) map for the selected options on the given
   * product. Falls back to zero for any selection not found in the cost projection.
   */
  private Map<Long, Money> loadExtraPricesByOptionId(Long productId, List<ProductOption> options) {
    if (options == null || options.isEmpty()) {
      return Map.of();
    }
    Currency cop = Currency.getInstance("COP");
    Map<Long, Money> result = new HashMap<>();
    try {
      List<ProductOptionCostProfile> profiles =
          productOptionRepositoryPort.loadCostProfilesByProductId(productId);
      for (ProductOptionCostProfile profile : profiles) {
        result.put(profile.optionId(), profile.extraPrice());
      }
    } catch (RuntimeException lookupFailure) {
      log.warn(
          "Could not load cost profiles for product {} (using 0 per-option extra_price): {}",
          productId,
          lookupFailure.getMessage());
    }
    for (ProductOption option : options) {
      result.putIfAbsent(option.getId(), Money.zero(cop));
    }
    return result;
  }
}
