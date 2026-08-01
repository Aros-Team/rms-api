/* (C) 2026 */

package aros.services.rms.core.product.domain;

import aros.services.rms.core.common.money.domain.Money;

/** Read projection containing option and category cost configuration for a product. */
public record ProductOptionCostProfile(
    Long optionId,
    String optionName,
    Long categoryId,
    String categoryName,
    Money extraPrice,
    String categorySelectionType,
    Long replaceSupplyCategoryId,
    Money defaultSlotCost) {}
