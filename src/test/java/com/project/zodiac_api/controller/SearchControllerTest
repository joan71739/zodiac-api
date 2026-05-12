package com.project.zodiac_api.controller;

import com.project.zodiac_api.dto.ClientResponseDto;
import com.project.zodiac_api.model.PlanetPosition;
import com.project.zodiac_api.repository.ClientRepository;
import com.project.zodiac_api.repository.PlanetPositionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SearchController.class)
@DisplayName("SearchController MockMvc 測試")
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlanetPositionRepository planetRepo;

    @MockBean
    private ClientRepository clientRepo;

    // ── 正常搜尋 ─────────────────────────────────────────

    @Test
    @DisplayName("GET /api/search?planet=太陽&sign=獅子座 — 200 回傳結果")
    void search_withRequiredParams_returns200() throws Exception {
        PlanetPosition pp = new PlanetPosition();
        pp.setClientId(1);

        ClientResponseDto clientDto = new ClientResponseDto();
        clientDto.setId(1);
        clientDto.setName("王小明");
        clientDto.setBirthDate(LocalDate.of(1993, 8, 10));
        clientDto.setBirthPlace("台北");

        when(planetRepo.search("太陽", "獅子座", null, null, null))
                .thenReturn(List.of(pp));
        when(clientRepo.findAllById(List.of(1)))
                .thenReturn(List.of(makeClient(1, "王小明")));

        mockMvc.perform(get("/api/search")
                        .param("planet", "太陽")
                        .param("sign", "獅子座"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("王小明"));
    }

    @Test
    @DisplayName("GET /api/search — 含所有選填參數 — 200")
    void search_withAllParams_returns200() throws Exception {
        when(planetRepo.search("月亮", "金牛座", (short) 10, (short) 20, 7))
                .thenReturn(List.of());
        when(clientRepo.findAllById(List.of())).thenReturn(List.of());

        mockMvc.perform(get("/api/search")
                        .param("planet", "月亮")
                        .param("sign", "金牛座")
                        .param("degreeFrom", "10")
                        .param("degreeTo", "20")
                        .param("house", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/search — 搜尋無結果時回傳空陣列")
    void search_noResults_returnsEmptyArray() throws Exception {
        when(planetRepo.search(anyString(), anyString(), any(), any(), any()))
                .thenReturn(List.of());
        when(clientRepo.findAllById(List.of())).thenReturn(List.of());

        mockMvc.perform(get("/api/search")
                        .param("planet", "冥王星")
                        .param("sign", "雙魚座"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ── 必填參數驗證 ─────────────────────────────────────

    @Test
    @DisplayName("GET /api/search — 缺少 planet — 400")
    void search_missingPlanet_returns400() throws Exception {
        mockMvc.perform(get("/api/search")
                        .param("sign", "獅子座"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/search — 缺少 sign — 400")
    void search_missingSign_returns400() throws Exception {
        mockMvc.perform(get("/api/search")
                        .param("planet", "太陽"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/search — 兩個必填都缺 — 400")
    void search_missingBoth_returns400() throws Exception {
        mockMvc.perform(get("/api/search"))
                .andExpect(status().isBadRequest());
    }

    // ── helper ──────────────────────────────────────────

    private com.project.zodiac_api.model.Client makeClient(int id, String name) {
        com.project.zodiac_api.model.Client c = new com.project.zodiac_api.model.Client();
        c.setId(id);
        c.setName(name);
        c.setBirthDate(LocalDate.of(1993, 8, 10));
        c.setBirthPlace("台北");
        return c;
    }
}
