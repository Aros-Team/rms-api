/* (C) 2026 */

package aros.services.rms.core.category.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link OptionCategory} domain invariants. */
class OptionCategoryDomainTest {

  @Test
  void should_default_selection_type_to_single_choice_when_null() {
    OptionCategory category =
        OptionCategory.builder().id(1L).name("Proteína").description("x").build();

    assertEquals(OptionSelectionType.SINGLE_CHOICE, category.getSelectionType());
    assertNull(category.getReplaceSupplyCategoryId());
  }

  @Test
  void should_allow_substitution_when_single_choice_and_replacement_present() {
    OptionCategory category =
        OptionCategory.builder()
            .id(2L)
            .name("Corte")
            .selectionType(OptionSelectionType.SINGLE_CHOICE)
            .replaceSupplyCategoryId(7L)
            .build();

    assertEquals(OptionSelectionType.SINGLE_CHOICE, category.getSelectionType());
    assertEquals(7L, category.getReplaceSupplyCategoryId());
  }

  @Test
  void should_clear_replacement_when_switching_to_extra() {
    OptionCategory category =
        OptionCategory.builder()
            .id(3L)
            .name("Extra")
            .selectionType(OptionSelectionType.SINGLE_CHOICE)
            .replaceSupplyCategoryId(7L)
            .build();
    assertEquals(7L, category.getReplaceSupplyCategoryId());

    category.setSelectionType(OptionSelectionType.EXTRA);

    assertEquals(OptionSelectionType.EXTRA, category.getSelectionType());
    assertNull(category.getReplaceSupplyCategoryId());
  }

  @Test
  void should_clear_replacement_when_switching_to_remove() {
    OptionCategory category =
        OptionCategory.builder()
            .id(4L)
            .name("Remove")
            .selectionType(OptionSelectionType.SINGLE_CHOICE)
            .replaceSupplyCategoryId(7L)
            .build();

    category.setSelectionType(OptionSelectionType.REMOVE);

    assertEquals(OptionSelectionType.REMOVE, category.getSelectionType());
    assertNull(category.getReplaceSupplyCategoryId());
  }

  @Test
  void should_clear_replacement_when_switching_to_multi_select() {
    OptionCategory category =
        OptionCategory.builder()
            .id(5L)
            .name("Multi")
            .selectionType(OptionSelectionType.SINGLE_CHOICE)
            .replaceSupplyCategoryId(7L)
            .build();

    category.setSelectionType(OptionSelectionType.MULTI_SELECT);

    assertEquals(OptionSelectionType.MULTI_SELECT, category.getSelectionType());
    assertNull(category.getReplaceSupplyCategoryId());
  }

  @Test
  void should_reject_setting_replacement_when_selection_type_is_extra() {
    OptionCategory category =
        OptionCategory.builder()
            .id(6L)
            .name("Extra")
            .selectionType(OptionSelectionType.EXTRA)
            .build();

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> category.setReplaceSupplyCategoryId(7L));
    assertTrue(ex.getMessage().contains("EXTRA"));
    assertTrue(ex.getMessage().contains("SINGLE_CHOICE"));
  }

  @Test
  void should_reject_setting_replacement_when_selection_type_is_remove() {
    OptionCategory category =
        OptionCategory.builder()
            .id(7L)
            .name("Remove")
            .selectionType(OptionSelectionType.REMOVE)
            .build();

    assertThrows(IllegalArgumentException.class, () -> category.setReplaceSupplyCategoryId(7L));
  }

  @Test
  void should_reject_setting_replacement_when_selection_type_is_multi_select() {
    OptionCategory category =
        OptionCategory.builder()
            .id(8L)
            .name("Multi")
            .selectionType(OptionSelectionType.MULTI_SELECT)
            .build();

    assertThrows(IllegalArgumentException.class, () -> category.setReplaceSupplyCategoryId(7L));
  }

  @Test
  void should_allow_null_replacement_on_extra() {
    OptionCategory category =
        OptionCategory.builder()
            .id(9L)
            .name("Extra")
            .selectionType(OptionSelectionType.EXTRA)
            .build();

    category.setReplaceSupplyCategoryId(null);

    assertNull(category.getReplaceSupplyCategoryId());
  }

  @Test
  void should_persist_selection_type_and_replacement_via_all_args_constructor() {
    OptionCategory category =
        new OptionCategory(10L, "Sub", "Substitution", OptionSelectionType.SINGLE_CHOICE, 99L);

    assertEquals(OptionSelectionType.SINGLE_CHOICE, category.getSelectionType());
    assertEquals(99L, category.getReplaceSupplyCategoryId());
  }

  @Test
  void should_reject_replacement_via_all_args_constructor_when_not_single_choice() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new OptionCategory(11L, "Bad", "Bad", OptionSelectionType.EXTRA, 1L));
  }
}
