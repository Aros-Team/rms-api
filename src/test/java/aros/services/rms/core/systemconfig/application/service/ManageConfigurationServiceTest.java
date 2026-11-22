/* (C) 2026 */

package aros.services.rms.core.systemconfig.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.systemconfig.domain.SystemConfiguration;
import aros.services.rms.core.systemconfig.domain.SystemConfigurationGroup;
import aros.services.rms.core.systemconfig.domain.exception.ConfigEntryNotFoundException;
import aros.services.rms.core.systemconfig.domain.exception.ConfigGroupAlreadyExistsException;
import aros.services.rms.core.systemconfig.domain.exception.ConfigGroupNotEmptyException;
import aros.services.rms.core.systemconfig.domain.exception.ConfigGroupNotFoundException;
import aros.services.rms.core.systemconfig.domain.port.input.ManageConfigurationUseCase.EntryUpdate;
import aros.services.rms.core.systemconfig.domain.port.input.ManageConfigurationUseCase.GroupCommand;
import aros.services.rms.core.systemconfig.domain.port.input.UpdateSystemConfigurationUseCase;
import aros.services.rms.core.systemconfig.domain.port.output.SystemConfigurationPort;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link ManageConfigurationService}.
 *
 * <p><b>Feature:</b> configuration group CRUD and per-entry operations.
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>Creates groups with the next sort order and rejects duplicate names
 *   <li>Renames and reorders groups, rejecting duplicates of other groups
 *   <li>Refuses to delete a group that still has entries
 *   <li>Updates and deletes only existing entries
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Mocks the configuration port and the legacy upsert use case
 *   <li>Asserts the exception type on every invalid transition
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class ManageConfigurationServiceTest {

  @Mock private SystemConfigurationPort systemConfigurationPort;
  @Mock private UpdateSystemConfigurationUseCase updateSystemConfigurationUseCase;

  private ManageConfigurationService service;

  @BeforeEach
  void setUp() {
    service =
        new ManageConfigurationService(systemConfigurationPort, updateSystemConfigurationUseCase);
  }

  @Test
  void should_create_group_with_next_sort_order() {
    when(systemConfigurationPort.findGroupByName("Notificaciones")).thenReturn(Optional.empty());
    when(systemConfigurationPort.findAllGroups())
        .thenReturn(List.of(group(1L, "General", 1), group(2L, "Nómina", 2)));
    when(systemConfigurationPort.saveGroup(any()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    SystemConfigurationGroup created =
        service.createGroup(new GroupCommand("Notificaciones", "Alertas", null));

    assertEquals("Notificaciones", created.getName());
    assertEquals(3, created.getSortOrder());
  }

  @Test
  void should_reject_duplicate_group_name() {
    when(systemConfigurationPort.findGroupByName("General"))
        .thenReturn(Optional.of(group(1L, "General", 1)));

    assertThrows(
        ConfigGroupAlreadyExistsException.class,
        () -> service.createGroup(new GroupCommand("General", null, 1)));

    verify(systemConfigurationPort, never()).saveGroup(any());
  }

  @Test
  void should_rename_group_when_name_is_free() {
    SystemConfigurationGroup existing = group(2L, "Nomina", 2);
    when(systemConfigurationPort.findGroupById(2L)).thenReturn(Optional.of(existing));
    when(systemConfigurationPort.findGroupByName("Nómina")).thenReturn(Optional.empty());
    when(systemConfigurationPort.saveGroup(any()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    SystemConfigurationGroup updated =
        service.updateGroup(2L, new GroupCommand("Nómina", "Nómina y pagos", 4));

    assertEquals("Nómina", updated.getName());
    assertEquals("Nómina y pagos", updated.getDescription());
    assertEquals(4, updated.getSortOrder());
  }

  @Test
  void should_reject_rename_to_another_groups_name() {
    when(systemConfigurationPort.findGroupById(2L)).thenReturn(Optional.of(group(2L, "Nomina", 2)));
    when(systemConfigurationPort.findGroupByName("General"))
        .thenReturn(Optional.of(group(1L, "General", 1)));

    assertThrows(
        ConfigGroupAlreadyExistsException.class,
        () -> service.updateGroup(2L, new GroupCommand("General", null, null)));

    verify(systemConfigurationPort, never()).saveGroup(any());
  }

  @Test
  void should_reject_delete_when_group_has_entries() {
    when(systemConfigurationPort.findGroupById(1L))
        .thenReturn(Optional.of(group(1L, "General", 1)));
    when(systemConfigurationPort.countEntriesInGroup(1L)).thenReturn(3);

    assertThrows(ConfigGroupNotEmptyException.class, () -> service.deleteGroup(1L));

    verify(systemConfigurationPort, never()).deleteGroup(1L);
  }

  @Test
  void should_delete_empty_group() {
    when(systemConfigurationPort.findGroupById(4L)).thenReturn(Optional.of(group(4L, "Extra", 4)));
    when(systemConfigurationPort.countEntriesInGroup(4L)).thenReturn(0);

    service.deleteGroup(4L);

    verify(systemConfigurationPort).deleteGroup(4L);
  }

  @Test
  void should_reject_delete_of_unknown_group() {
    when(systemConfigurationPort.findGroupById(99L)).thenReturn(Optional.empty());

    assertThrows(ConfigGroupNotFoundException.class, () -> service.deleteGroup(99L));
  }

  @Test
  void should_update_existing_entry() {
    SystemConfiguration existing =
        SystemConfiguration.builder().id(2L).key("default_currency").value("COP").build();
    when(systemConfigurationPort.findByKey("default_currency")).thenReturn(Optional.of(existing));
    when(updateSystemConfigurationUseCase.save(any()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    SystemConfiguration saved =
        service.updateEntry("default_currency", new EntryUpdate("USD", null, 1L, 5));

    assertEquals("USD", saved.getValue());
    assertEquals(1L, saved.getGroupId());
    assertEquals(5, saved.getSortOrder());
    ArgumentCaptor<SystemConfiguration> captor = ArgumentCaptor.forClass(SystemConfiguration.class);
    verify(updateSystemConfigurationUseCase).save(captor.capture());
    assertEquals("USD", captor.getValue().getValue());
  }

  @Test
  void should_reject_update_of_unknown_entry() {
    when(systemConfigurationPort.findByKey("missing")).thenReturn(Optional.empty());

    assertThrows(
        ConfigEntryNotFoundException.class,
        () -> service.updateEntry("missing", new EntryUpdate("x", null, null, null)));
  }

  @Test
  void should_delete_existing_entry() {
    when(systemConfigurationPort.findByKey("legacy"))
        .thenReturn(Optional.of(SystemConfiguration.builder().key("legacy").build()));

    service.deleteEntry("legacy");

    verify(systemConfigurationPort).deleteByKey("legacy");
  }

  @Test
  void should_reject_delete_of_unknown_entry() {
    when(systemConfigurationPort.findByKey("missing")).thenReturn(Optional.empty());

    assertThrows(ConfigEntryNotFoundException.class, () -> service.deleteEntry("missing"));

    verify(systemConfigurationPort, never()).deleteByKey("missing");
  }

  private static SystemConfigurationGroup group(Long id, String name, int sortOrder) {
    return SystemConfigurationGroup.builder().id(id).name(name).sortOrder(sortOrder).build();
  }
}
