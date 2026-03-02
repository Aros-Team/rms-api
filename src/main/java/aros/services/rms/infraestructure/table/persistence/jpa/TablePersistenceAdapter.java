package aros.services.rms.infraestructure.table.persistence.jpa;

import aros.services.rms.core.table.domain.Table;
import aros.services.rms.core.table.domain.TableStatus;
import aros.services.rms.core.table.port.output.TableRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TablePersistenceAdapter implements TableRepositoryPort {

    private final TableRepository tableRepository;
    private final TableMapper tableMapper;

    @Override
    public Optional<Table> findById(Long id) {
        return tableRepository.findById(id).map(tableMapper::toDomain);
    }

    @Override
    public Table save(Table table) {
        aros.services.rms.infraestructure.table.persistence.Table entity = tableMapper.toEntity(table);
        aros.services.rms.infraestructure.table.persistence.Table savedEntity = tableRepository.save(entity);
        return tableMapper.toDomain(savedEntity);
    }
}