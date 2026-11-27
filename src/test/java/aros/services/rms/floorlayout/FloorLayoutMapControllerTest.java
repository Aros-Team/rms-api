/* (C) 2026 */

package aros.services.rms.floorlayout;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.floorlayout.domain.FloorLayout;
import aros.services.rms.core.floorlayout.domain.FloorRegion;
import aros.services.rms.core.floorlayout.domain.FloorSegment;
import aros.services.rms.core.floorlayout.domain.FloorTablePlacement;
import aros.services.rms.core.floorlayout.port.input.FloorLayoutUseCase;
import aros.services.rms.infraestructure.common.dto.ApiEnvelopeAdvice;
import aros.services.rms.infraestructure.floorlayout.api.FloorLayoutMapController;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** Tests for {@link FloorLayoutMapController}. */
@ExtendWith(MockitoExtension.class)
class FloorLayoutMapControllerTest {

  @Mock private FloorLayoutUseCase floorLayoutUseCase;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new FloorLayoutMapController(floorLayoutUseCase))
            .setControllerAdvice(new ApiEnvelopeAdvice())
            .build();
  }

  @Test
  void shouldReturnLayoutAndPlacementsForTheOperationalMap() throws Exception {
    when(floorLayoutUseCase.get())
        .thenReturn(
            FloorLayout.builder()
                .columns(12)
                .rows(10)
                .regions(
                    List.of(
                        FloorRegion.builder()
                            .key("r1")
                            .areaId(2L)
                            .segments(List.of(new FloorSegment(4, 1, 3)))
                            .build()))
                .build());
    when(floorLayoutUseCase.getTablePlacements())
        .thenReturn(List.of(FloorTablePlacement.builder().tableId(7L).row(4).column(2).build()));

    mockMvc
        .perform(get("/api/v1/floor-layout/map"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.layout.columns").value(12))
        .andExpect(jsonPath("$.data.layout.regions[0].areaId").value(2))
        .andExpect(jsonPath("$.data.tables[0].tableId").value(7))
        .andExpect(jsonPath("$.data.tables[0].row").value(4))
        .andExpect(jsonPath("$.data.tables[0].column").value(2));
  }
}
