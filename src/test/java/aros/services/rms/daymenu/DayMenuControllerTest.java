/* (C) 2026 */
package aros.services.rms.daymenu;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import aros.services.rms.core.daymenu.domain.DayMenu;
import aros.services.rms.core.daymenu.port.input.GetCurrentDayMenuUseCase;
import aros.services.rms.core.daymenu.port.input.GetDayMenuHistoryUseCase;
import aros.services.rms.core.daymenu.port.input.UpdateDayMenuUseCase;
import aros.services.rms.infraestructure.daymenu.api.DayMenuController;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DayMenuController.class)
class DayMenuControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private UpdateDayMenuUseCase updateDayMenuUseCase;
  @MockitoBean private GetCurrentDayMenuUseCase getCurrentDayMenuUseCase;
  @MockitoBean private GetDayMenuHistoryUseCase getDayMenuHistoryUseCase;

  @Test
  @WithMockUser(username = "admin")
  void shouldReturn200WhenCurrentDayMenuExists() throws Exception {
    var dayMenu =
        DayMenu.builder()
            .id(1L)
            .productId(5L)
            .productName("Menú Especial")
            .productBasePrice(15.0)
            .validFrom(LocalDateTime.now())
            .createdBy("admin")
            .build();

    when(getCurrentDayMenuUseCase.getCurrent()).thenReturn(Optional.of(dayMenu));

    mockMvc
        .perform(get("/api/v1/day-menu/current"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.productId").value(5L))
        .andExpect(jsonPath("$.productName").value("Menú Especial"));
  }

  @Test
  @WithMockUser(username = "admin")
  void shouldReturn204WhenNoDayMenuConfigured() throws Exception {
    when(getCurrentDayMenuUseCase.getCurrent()).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/v1/day-menu/current")).andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(username = "admin")
  void shouldReturn200WhenUpdateSucceeds() throws Exception {
    var dayMenu =
        DayMenu.builder()
            .id(1L)
            .productId(5L)
            .productName("Menú Especial")
            .productBasePrice(15.0)
            .validFrom(LocalDateTime.now())
            .createdBy("admin")
            .build();

    when(updateDayMenuUseCase.update(anyLong(), anyString())).thenReturn(dayMenu);

    mockMvc
        .perform(
            put("/api/v1/day-menu")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\": 5}")
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.productId").value(5L));
  }

  @Test
  @WithMockUser(username = "admin")
  void shouldReturn400WhenProductIdIsNull() throws Exception {
    mockMvc
        .perform(
            put("/api/v1/day-menu")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\": null}")
                .with(csrf()))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(username = "admin")
  void shouldReturn400WhenPageIsNegative() throws Exception {
    when(getDayMenuHistoryUseCase.getHistory(any(Pageable.class))).thenReturn(Page.empty());

    mockMvc
        .perform(get("/api/v1/day-menu/history").param("page", "-1"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(username = "admin")
  void shouldReturn400WhenSizeExceedsMax() throws Exception {
    when(getDayMenuHistoryUseCase.getHistory(any(Pageable.class))).thenReturn(Page.empty());

    mockMvc
        .perform(get("/api/v1/day-menu/history").param("size", "101"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(username = "admin")
  void shouldReturn200WithEmptyHistoryPage() throws Exception {
    when(getDayMenuHistoryUseCase.getHistory(any(Pageable.class))).thenReturn(Page.empty());

    mockMvc
        .perform(get("/api/v1/day-menu/history").param("page", "0").param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(0));
  }
}
