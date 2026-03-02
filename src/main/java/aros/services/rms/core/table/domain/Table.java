package aros.services.rms.core.table.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Table {
    private Long id;
    private Integer tableNumber;
    private Integer capacity;
    private TableStatus status;
}