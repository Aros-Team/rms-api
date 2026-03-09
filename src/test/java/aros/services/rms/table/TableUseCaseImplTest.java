/* (C) 2026 */
package aros.services.rms.table;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import aros.services.rms.core.common.logger.Logger;
import aros.services.rms.core.table.application.exception.InvalidTableStatusException;
import aros.services.rms.core.table.application.exception.TableNotFoundException;
import aros.services.rms.core.table.application.usecases.TableUseCaseImpl;
import aros.services.rms.core.table.domain.Table;
import aros.services.rms.core.table.domain.TableStatus;
import aros.services.rms.core.table.port.output.TableRepositoryPort;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TableUseCaseImplTest {

  @Mock private TableRepositoryPort tableRepositoryPort;
  @Mock private Logger logger;

  private TableUseCaseImpl tableUseCase;

  @BeforeEach
  void setUp() {
    tableUseCase = new TableUseCaseImpl(tableRepositoryPort, logger);
  }

  @Test
  void shouldCreateTableSuccessfully() {
    Table table = Table.builder().tableNumber(1).capacity(4).build();
    Table saved =
        Table.builder().id(1L).tableNumber(1).capacity(4).status(TableStatus.AVAILABLE).build();

    when(tableRepositoryPort.save(any(Table.class))).thenReturn(saved);

    Table result = tableUseCase.create(table);

    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals(TableStatus.AVAILABLE, result.getStatus());
  }

  @Test
  void shouldUpdateTableSuccessfully() {
    Table existing =
        Table.builder().id(1L).tableNumber(1).capacity(4).status(TableStatus.AVAILABLE).build();
    Table updateData = Table.builder().tableNumber(2).capacity(6).build();
    Table saved =
        Table.builder().id(1L).tableNumber(2).capacity(6).status(TableStatus.AVAILABLE).build();

    when(tableRepositoryPort.findById(1L)).thenReturn(Optional.of(existing));
    when(tableRepositoryPort.save(any(Table.class))).thenReturn(saved);

    Table result = tableUseCase.update(1L, updateData);

    assertEquals(2, result.getTableNumber());
    assertEquals(6, result.getCapacity());
  }

  @Test
  void shouldThrowWhenUpdatingNonExistentTable() {
    when(tableRepositoryPort.findById(99L)).thenReturn(Optional.empty());

    assertThrows(
        TableNotFoundException.class,
        () -> tableUseCase.update(99L, Table.builder().tableNumber(1).capacity(4).build()));
  }

  @Test
  void shouldChangeStatusSuccessfully() {
    Table existing =
        Table.builder().id(1L).tableNumber(1).capacity(4).status(TableStatus.AVAILABLE).build();
    Table saved =
        Table.builder().id(1L).tableNumber(1).capacity(4).status(TableStatus.OCCUPIED).build();

    when(tableRepositoryPort.findById(1L)).thenReturn(Optional.of(existing));
    when(tableRepositoryPort.save(any(Table.class))).thenReturn(saved);

    Table result = tableUseCase.changeStatus(1L, TableStatus.OCCUPIED);

    assertEquals(TableStatus.OCCUPIED, result.getStatus());
  }

  @Test
  void shouldChangeStatusToReserved() {
    Table existing =
        Table.builder().id(1L).tableNumber(1).capacity(4).status(TableStatus.AVAILABLE).build();
    Table saved =
        Table.builder().id(1L).tableNumber(1).capacity(4).status(TableStatus.RESERVED).build();

    when(tableRepositoryPort.findById(1L)).thenReturn(Optional.of(existing));
    when(tableRepositoryPort.save(any(Table.class))).thenReturn(saved);

    Table result = tableUseCase.changeStatus(1L, TableStatus.RESERVED);

    assertEquals(TableStatus.RESERVED, result.getStatus());
  }

  @Test
  void shouldThrowWhenChangingStatusWithNull() {
    Table existing =
        Table.builder().id(1L).tableNumber(1).capacity(4).status(TableStatus.AVAILABLE).build();

    when(tableRepositoryPort.findById(1L)).thenReturn(Optional.of(existing));

    assertThrows(InvalidTableStatusException.class, () -> tableUseCase.changeStatus(1L, null));
  }

  @Test
  void shouldThrowWhenChangingStatusOfNonExistentTable() {
    when(tableRepositoryPort.findById(99L)).thenReturn(Optional.empty());

    assertThrows(
        TableNotFoundException.class, () -> tableUseCase.changeStatus(99L, TableStatus.OCCUPIED));
  }

  @Test
  void shouldFindAllTables() {
    when(tableRepositoryPort.findAll())
        .thenReturn(
            List.of(
                Table.builder().id(1L).tableNumber(1).build(),
                Table.builder().id(2L).tableNumber(2).build()));

    List<Table> result = tableUseCase.findAll();

    assertEquals(2, result.size());
  }

  @Test
  void shouldFindTableById() {
    Table table = Table.builder().id(1L).tableNumber(1).capacity(4).build();

    when(tableRepositoryPort.findById(1L)).thenReturn(Optional.of(table));

    Table result = tableUseCase.findById(1L);

    assertEquals(1L, result.getId());
  }
}
