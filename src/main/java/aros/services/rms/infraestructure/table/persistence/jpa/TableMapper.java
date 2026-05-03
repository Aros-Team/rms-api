/* (C) 2026 */

package aros.services.rms.infraestructure.table.persistence.jpa;

import aros.services.rms.core.table.domain.Table;
import aros.services.rms.core.table.domain.TableStatus;
import org.springframework.stereotype.Component;

/** Mapper for Table entities. */
@Component
public class TableMapper {

  /** Converts a Table entity to a domain Table. */
  public Table toDomain(aros.services.rms.infraestructure.table.persistence.Table entity) {
    if (entity == null) {
      return null;
    }
    return Table.builder()
        .id(entity.getId())
        .tableNumber(entity.getTableNumber())
        .capacity(entity.getCapacity())
        .status(entity.getStatus() != null ? TableStatus.valueOf(entity.getStatus().name()) : null)
        .build();
  }

  /** Converts a domain Table to a Table entity. */
  public aros.services.rms.infraestructure.table.persistence.Table toEntity(Table domain) {
    if (domain == null) {
      return null;
    }
    return aros.services.rms.infraestructure.table.persistence.Table.builder()
        .id(domain.getId())
        .tableNumber(domain.getTableNumber())
        .capacity(domain.getCapacity())
        .status(
            domain.getStatus() != null
                ? aros.services.rms.infraestructure.table.persistence.TableStatus.valueOf(
                    domain.getStatus().name())
                : null)
        .build();
  }
}
