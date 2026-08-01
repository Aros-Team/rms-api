/* (C) 2026 */

package aros.services.rms.core.category.domain;

/**
 * Selection mode of an {@link OptionCategory}.
 *
 * <p>An option category groups customization options offered to a customer (e.g. "Proteína",
 * "Queso", "Adición"). The selection mode tells the order-entry logic how many options from the
 * category may be chosen and what they mean economically:
 *
 * <ul>
 *   <li>{@link #SINGLE_CHOICE} — exactly one option may be chosen. May carry a {@code
 *       replaceSupplyCategoryId} on the category to model a substitution swap (e.g. "Salsa tártara"
 *       in place of the base recipe's "Mayonesa").
 *   <li>{@link #MULTI_SELECT} — zero or more options may be chosen. Each option adds its material
 *       cost independently.
 *   <li>{@link #EXTRA} — standalone surcharge (e.g. "Extra queso"). Each EXTRA option carries its
 *       own {@code extraPrice} and contributes to the order line price independently. Excluded from
 *       projected effective cost.
 *   <li>{@link #REMOVE} — subtractive option (e.g. "Sin cebolla"). The recipe of a chosen REMOVE
 *       option is subtracted from inventory deduction and the menu-engineering average option cost.
 *       Excluded from projected effective cost.
 * </ul>
 *
 * <p>Persisted as a {@code VARCHAR} via {@code @Enumerated(EnumType.STRING)}. The stored value MUST
 * be the enum constant name (e.g. {@code "SINGLE_CHOICE"}).
 */
public enum OptionSelectionType {
  SINGLE_CHOICE,
  MULTI_SELECT,
  EXTRA,
  REMOVE
}
