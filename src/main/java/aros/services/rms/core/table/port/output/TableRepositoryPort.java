package aros.services.rms.core.table.port.output;

import aros.services.rms.core.table.domain.Table;
import java.util.Optional;

public interface TableRepositoryPort {
    Optional<Table> findById(Long id);
    Table save(Table table);
}