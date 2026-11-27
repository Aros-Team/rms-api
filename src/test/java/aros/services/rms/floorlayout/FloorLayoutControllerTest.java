/* (C) 2026 */

package aros.services.rms.floorlayout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.floorlayout.domain.FloorLayout;
import aros.services.rms.core.floorlayout.domain.FloorRegion;
import aros.services.rms.core.floorlayout.domain.FloorSegment;
import aros.services.rms.core.floorlayout.domain.FloorTablePlacement;
import aros.services.rms.core.floorlayout.port.input.FloorLayoutUseCase;
import aros.services.rms.infraestructure.common.dto.ApiEnvelopeAdvice;
import aros.services.rms.infraestructure.floorlayout.api.FloorLayoutController;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** Tests for {@link FloorLayoutController}. */
@ExtendWith(MockitoExtension.class)
class FloorLayoutControllerTest {

  @Mock private FloorLayoutUseCase floorLayoutUseCase;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new FloorLayoutController(floorLayoutUseCase))
            .setControllerAdvice(new ApiEnvelopeAdvice())
            .build();
  }

  @Test
  void shouldReturnTheWrappedLayout() throws Exception {
    when(floorLayoutUseCase.get())
        .thenReturn(
            FloorLayout.builder()
                .columns(32)
                .rows(20)
                .regions(
                    List.of(
                        FloorRegion.builder()
                            .key("r1")
                            .areaId(3L)
                            .segments(List.of(new FloorSegment(1, 2, 4)))
                            .build()))
                .updatedAt(Instant.parse("2026-11-26T15:30:00Z"))
                .build());

    mockMvc
        .perform(get("/api/v1/floor-layout"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.columns").value(32))
        .andExpect(jsonPath("$.data.rows").value(20))
        .andExpect(jsonPath("$.data.regions[0].key").value("r1"))
        .andExpect(jsonPath("$.data.regions[0].areaId").value(3))
        .andExpect(jsonPath("$.data.regions[0].segments[0].row").value(1))
        .andExpect(jsonPath("$.data.regions[0].segments[0].from").value(2))
        .andExpect(jsonPath("$.data.regions[0].segments[0].to").value(4));
  }

  @Test
  void shouldMapTheRequestBodyToTheDomainAndReturnTheSavedLayout() throws Exception {
    when(floorLayoutUseCase.save(any(FloorLayout.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    String body =
        """
        {
          "columns": 40,
          "rows": 24,
          "regions": [
            {
              "key": "r1",
              "areaId": 3,
              "segments": [{"row": 1, "from": 2, "to": 4}]
            }
          ]
        }
        """;

    mockMvc
        .perform(put("/api/v1/floor-layout").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.columns").value(40))
        .andExpect(jsonPath("$.data.regions[0].areaId").value(3));

    ArgumentCaptor<FloorLayout> captor = ArgumentCaptor.forClass(FloorLayout.class);
    org.mockito.Mockito.verify(floorLayoutUseCase).save(captor.capture());
    assertEquals(40, captor.getValue().getColumns());
    assertEquals(24, captor.getValue().getRows());
    assertEquals(1, captor.getValue().getRegions().size());
    assertEquals("r1", captor.getValue().getRegions().get(0).getKey());
    assertEquals(
        new FloorSegment(1, 2, 4), captor.getValue().getRegions().get(0).getSegments().get(0));
  }

  @Test
  void shouldReturnTablePlacements() throws Exception {
    when(floorLayoutUseCase.getTablePlacements())
        .thenReturn(List.of(FloorTablePlacement.builder().tableId(7L).row(2).column(3).build()));

    mockMvc
        .perform(get("/api/v1/floor-layout/tables"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].tableId").value(7))
        .andExpect(jsonPath("$.data[0].row").value(2))
        .andExpect(jsonPath("$.data[0].column").value(3));
  }

  @Test
  void shouldMapTablePlacementRequestsToTheDomain() throws Exception {
    when(floorLayoutUseCase.saveTablePlacements(any()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    mockMvc
        .perform(
            put("/api/v1/floor-layout/tables")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[{\"tableId\":7,\"row\":2,\"column\":3}]"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].tableId").value(7));

    ArgumentCaptor<List<FloorTablePlacement>> captor = ArgumentCaptor.forClass(List.class);
    org.mockito.Mockito.verify(floorLayoutUseCase).saveTablePlacements(captor.capture());
    assertEquals(7L, captor.getValue().get(0).getTableId());
    assertEquals(2, captor.getValue().get(0).getRow());
    assertEquals(3, captor.getValue().get(0).getColumn());
  }
}
