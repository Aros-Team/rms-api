/* (C) 2026 */

package aros.services.rms.infraestructure.specialselection.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import aros.services.rms.core.specialselection.domain.SpecialSelectionGroup;
import aros.services.rms.infraestructure.specialselection.persistence.GroupProductEntity;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SpecialSelectionMapper#toGroupProductLinks}.
 *
 * <p><b>Feature:</b> Group-product links of a special selection configuration.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>Links are matched positionally against the freshly saved group ids, so client-echoed ids of
 *       deleted rows are ignored
 *   <li>Groups without products produce no links
 *   <li>Duplicate product ids inside one group are persisted once
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Builds domain groups carrying stale ids, exactly as the admin request does
 *   <li>Passes the ids of the newly saved groups and asserts the emitted links
 *   <li>Asserts the persisted links point at the saved ids, never at the stale ones
 * </ul>
 */
class SpecialSelectionMapperTest {

  private final SpecialSelectionMapper mapper = new SpecialSelectionMapper();

  @Test
  void should_link_products_to_saved_group_ids_even_when_client_sends_stale_ids() {
    List<SpecialSelectionGroup> groups =
        List.of(
            SpecialSelectionGroup.builder().id(1L).productIds(List.of(10L, 11L)).build(),
            SpecialSelectionGroup.builder().id(2L).productIds(List.of(20L)).build());

    List<GroupProductEntity> links = mapper.toGroupProductLinks(99L, groups, List.of(101L, 102L));

    assertEquals(3, links.size());
    assertEquals(
        List.of(101L, 101L, 102L), links.stream().map(GroupProductEntity::getGroupId).toList());
    assertEquals(
        List.of(10L, 11L, 20L), links.stream().map(GroupProductEntity::getProductId).toList());
  }

  @Test
  void should_skip_groups_without_products() {
    List<SpecialSelectionGroup> groups =
        List.of(
            SpecialSelectionGroup.builder().id(1L).productIds(List.of()).build(),
            SpecialSelectionGroup.builder().id(2L).productIds(List.of(20L, 20L)).build());

    List<GroupProductEntity> links = mapper.toGroupProductLinks(99L, groups, List.of(101L, 102L));

    assertEquals(1, links.size());
    assertEquals(102L, links.getFirst().getGroupId());
    assertEquals(20L, links.getFirst().getProductId());
  }

  @Test
  void should_return_no_links_when_no_groups_were_saved() {
    List<SpecialSelectionGroup> groups =
        List.of(SpecialSelectionGroup.builder().id(1L).productIds(List.of(10L)).build());

    assertTrue(mapper.toGroupProductLinks(99L, groups, List.of()).isEmpty());
  }
}
