/* (C) 2026 */
package aros.services.rms.daymenu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import aros.services.rms.core.daymenu.application.service.GetDayMenuHistoryService;
import aros.services.rms.core.daymenu.domain.DayMenuHistory;
import aros.services.rms.core.daymenu.port.output.DayMenuHistoryRepositoryPort;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class GetDayMenuHistoryUseCaseImplTest {

  @Mock private DayMenuHistoryRepositoryPort dayMenuHistoryRepositoryPort;

  private GetDayMenuHistoryService useCase;

  @BeforeEach
  void setUp() {
    useCase = new GetDayMenuHistoryService(dayMenuHistoryRepositoryPort);
  }

  @Test
  void shouldReturnEmptyPageWhenHistoryIsEmpty() {
    when(dayMenuHistoryRepositoryPort.findAll(any(Pageable.class)))
        .thenReturn(Page.empty());

    var result = useCase.getHistory(PageRequest.of(0, 10));

    assertNotNull(result);
    assertEquals(0, result.getTotalElements());
  }

  @Test
  void shouldReturnPaginatedHistoryRecords() {
    var now = LocalDateTime.now();
    var records = List.of(
        DayMenuHistory.builder().id(2L).productId(2L).productName("Menú B").validFrom(now.minusDays(1)).validUntil(now).createdBy("chef").build(),
        DayMenuHistory.builder().id(1L).productId(1L).productName("Menú A").validFrom(now.minusDays(2)).validUntil(now.minusDays(1)).createdBy("admin").build()
    );
    var page = new PageImpl<>(records, PageRequest.of(0, 10), 2);

    when(dayMenuHistoryRepositoryPort.findAll(any(Pageable.class))).thenReturn(page);

    var result = useCase.getHistory(PageRequest.of(0, 10));

    assertEquals(2, result.getTotalElements());
    assertEquals(2L, result.getContent().get(0).getId());
    assertEquals(1L, result.getContent().get(1).getId());
  }
}
