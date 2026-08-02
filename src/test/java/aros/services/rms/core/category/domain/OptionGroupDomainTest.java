/* (C) 2026 */

package aros.services.rms.core.category.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link OptionGroup} domain invariants. */
class OptionGroupDomainTest {

  @Test
  void should_default_selection_type_to_single_choice_when_null() {
    OptionGroup category = OptionGroup.builder().id(1L).name("Proteína").description("x").build();

    assertEquals(OptionSelectionType.SINGLE_CHOICE, category.getSelectionType());
    assertNull(category.getReplaceSupplyCategoryId());
  }

  @Test
  void should_allow_substitution_when_single_choice_and_replacement_present() {
    OptionGroup category =
        OptionGroup.builder()
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
    OptionGroup category =
        OptionGroup.builder()
            .id(3L)
            .name("Extra")
            .selectionType(OptionSelectionType.SINGLE_CHOICE)
            .replaceSupplyCategoryId(7L)
            .build();
    assertEquals(7L, category.getReplaceSupplyCategoryId());

    category.setSelectionType(OptionSelectionType.ADD_ON);

    assertEquals(OptionSelectionType.ADD_ON, category.getSelectionType());
    assertNull(category.getReplaceSupplyCategoryId());
  }

  @Test
  void should_clear_replacement_when_switching_to_remove() {
    OptionGroup category =
        OptionGroup.builder()
            .id(4L)
            .name("Remove")
            .selectionType(OptionSelectionType.SINGLE_CHOICE)
            .replaceSupplyCategoryId(7L)
            .build();

    category.setSelectionType(OptionSelectionType.REMOVAL);

    assertEquals(OptionSelectionType.REMOVAL, category.getSelectionType());
    assertNull(category.getReplaceSupplyCategoryId());
  }

  @Test
  void should_clear_replacement_when_switching_to_multi_select() {
    OptionGroup category =
        OptionGroup.builder()
            .id(5L)
            .name("Multi")
            .selectionType(OptionSelectionType.SINGLE_CHOICE)
            .replaceSupplyCategoryId(7L)
            .build();

    category.setSelectionType(OptionSelectionType.MULTI_CHOICE);

    assertEquals(OptionSelectionType.MULTI_CHOICE, category.getSelectionType());
    assertNull(category.getReplaceSupplyCategoryId());
  }

  @Test
  void should_reject_setting_replacement_when_selection_type_is_extra() {
    OptionGroup category =
        OptionGroup.builder()
            .id(6L)
            .name("Extra")
            .selectionType(OptionSelectionType.ADD_ON)
            .build();

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> category.setReplaceSupplyCategoryId(7L));
    assertTrue(ex.getMessage().contains("ADD_ON"));
    assertTrue(ex.getMessage().contains("SINGLE_CHOICE"));
  }

  @Test
  void should_reject_setting_replacement_when_selection_type_is_remove() {
    OptionGroup category =
        OptionGroup.builder()
            .id(7L)
            .name("Remove")
            .selectionType(OptionSelectionType.REMOVAL)
            .build();

    assertThrows(IllegalArgumentException.class, () -> category.setReplaceSupplyCategoryId(7L));
  }

  @Test
  void should_reject_setting_replacement_when_selection_type_is_multi_select() {
    OptionGroup category =
        OptionGroup.builder()
            .id(8L)
            .name("Multi")
            .selectionType(OptionSelectionType.MULTI_CHOICE)
            .build();

    assertThrows(IllegalArgumentException.class, () -> category.setReplaceSupplyCategoryId(7L));
  }

  @Test
  void should_allow_null_replacement_on_extra() {
    OptionGroup category =
        OptionGroup.builder()
            .id(9L)
            .name("Extra")
            .selectionType(OptionSelectionType.ADD_ON)
            .build();

    category.setReplaceSupplyCategoryId(null);

    assertNull(category.getReplaceSupplyCategoryId());
  }

  @Test
  void should_persist_selection_type_and_replacement_via_all_args_constructor() {
    OptionGroup category =
        new OptionGroup(10L, "Sub", "Substitution", OptionSelectionType.SINGLE_CHOICE, 99L);

    assertEquals(OptionSelectionType.SINGLE_CHOICE, category.getSelectionType());
    assertEquals(99L, category.getReplaceSupplyCategoryId());
  }

  @Test
  void should_reject_replacement_via_all_args_constructor_when_not_single_choice() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new OptionGroup(11L, "Bad", "Bad", OptionSelectionType.ADD_ON, 1L));
  }
}
