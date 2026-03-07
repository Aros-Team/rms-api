package aros.services.rms.infraestructure.table.persistence.jpa;

import aros.services.rms.infraestructure.table.persistence.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TableRepository extends JpaRepository<Table, Long> {
}