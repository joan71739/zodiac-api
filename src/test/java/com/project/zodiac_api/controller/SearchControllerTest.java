package com.project.zodiac_api.controller;

import com.project.zodiac_api.dto.ClientResponseDto;
import com.project.zodiac_api.model.Client;
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

    // ── 命主星特殊分支（v8 核心業務邏輯）───────────────────

    @Test
    @DisplayName("GET /api/search?planet=命主星&sign=獅子座 — JPQL 轉 isLord=TRUE — 200")
    void search_withLordStar_returnsMatchingClients() throws Exception {
        // 命主星搜尋：前端傳 planet=命主星，Repository JPQL 自動轉查 is_lord = TRUE
        PlanetPosition pp = new PlanetPosition();
        pp.setClientId(2);
        pp.setIsLord(true);

        when(planetRepo.search("命主星", "獅子座", null, null, null))
                .thenReturn(List.of(pp));
        when(clientRepo.findAllById(List.of(2)))
                .thenReturn(List.of(makeClient(2, "陳大文")));

        mockMvc.perform(get("/api/search")
                        .param("planet", "命主星")
                        .param("sign", "獅子座"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("陳大文"));
    }

    @Test
    @DisplayName("GET /api/search?planet=命主星&sign=牡羊座 — 命主星無符合結果 — 200 空陣列")
    void search_withLordStar_noResults_returnsEmptyArray() throws Exception {
        when(planetRepo.search("命主星", "牡羊座", null, null, null))
                .thenReturn(List.of());
        when(clientRepo.findAllById(List.of())).thenReturn(List.of());

        mockMvc.perform(get("/api/search")
                        .param("planet", "命主星")
                        .param("sign", "牡羊座"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ── 必填參數驗證：缺少參數（Spring MissingServletRequestParameterException → 400）──

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

    // ── 必填參數驗證：空字串（Controller isBlank() → IllegalArgumentException → 400）──

    @Test
    @DisplayName("GET /api/search — planet 為空字串 — 400（isBlank 攔截）")
    void search_blankPlanet_returns400() throws Exception {
        // 前端下拉未選擇時傳 planet=""，Spring 不攔（參數存在），由 Controller isBlank() 丟 IllegalArgumentException
        mockMvc.perform(get("/api/search")
                        .param("planet", "")
                        .param("sign", "獅子座"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/search — sign 為空字串 — 400（isBlank 攔截）")
    void search_blankSign_returns400() throws Exception {
        mockMvc.perform(get("/api/search")
                        .param("planet", "太陽")
                        .param("sign", ""))
                .andExpect(status().isBadRequest());
    }

    // ── helper ──────────────────────────────────────────

    private Client makeClient(int id, String name) {
        Client c = new Client();
        c.setId(id);
        c.setName(name);
        c.setBirthDate(LocalDate.of(1993, 8, 10));
        c.setBirthPlace("台北");
        return c;
    }
}