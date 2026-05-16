package com.project.zodiac_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.zodiac_api.dto.PlanetPositionDto;
import com.project.zodiac_api.exception.ResourceNotFoundException;
import com.project.zodiac_api.service.PlanetPositionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlanetPositionController.class)
@DisplayName("PlanetPositionController MockMvc 測試")
class PlanetPositionControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean  PlanetPositionService planetService;

    // ────────────────────────────────────────────────────
    // GET /api/clients/{clientId}/planets
    // ────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/clients/1/planets — 200 回傳行星列表，含 isLord 欄位")
    void getAll_returns200WithIsLord() throws Exception {
        PlanetPositionDto sun  = makeDto(1, "太陽",  false);
        PlanetPositionDto venus = makeDto(2, "金星", true);
        when(planetService.getByClientId(1)).thenReturn(List.of(sun, venus));

        mockMvc.perform(get("/api/clients/1/planets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].planet").value("太陽"))
                .andExpect(jsonPath("$[0].isLord").value(false))
                .andExpect(jsonPath("$[1].planet").value("金星"))
                .andExpect(jsonPath("$[1].isLord").value(true));
    }

    @Test
    @DisplayName("GET /api/clients/999/planets — 404 client 不存在")
    void getAll_clientNotFound_returns404() throws Exception {
        when(planetService.getByClientId(999))
                .thenThrow(new ResourceNotFoundException("Client", 999));

        mockMvc.perform(get("/api/clients/999/planets"))
                .andExpect(status().isNotFound());
    }

    // ────────────────────────────────────────────────────
    // POST /api/clients/{clientId}/planets（整批建立）
    // ────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/clients/1/planets — 201 整批建立成功，回傳含 id 與 isLord")
    void createBatch_returns201() throws Exception {
        PlanetPositionDto req = makeDto(null, "金星", true);
        PlanetPositionDto saved = makeDto(10, "金星", true);
        when(planetService.createBatch(eq(1), anyList())).thenReturn(List.of(saved));

        mockMvc.perform(post("/api/clients/1/planets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(req))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].planet").value("金星"))
                .andExpect(jsonPath("$[0].isLord").value(true));
    }

    @Test
    @DisplayName("POST /api/clients/999/planets — 404 client 不存在")
    void createBatch_clientNotFound_returns404() throws Exception {
        when(planetService.createBatch(eq(999), anyList()))
                .thenThrow(new ResourceNotFoundException("Client", 999));

        mockMvc.perform(post("/api/clients/999/planets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(makeDto(null, "太陽", false)))))
                .andExpect(status().isNotFound());
    }

    // ────────────────────────────────────────────────────
    // PUT /api/clients/{clientId}/planets/{pid}（單筆編輯）
    // ────────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/clients/1/planets/5 — 200 isLord=true 編輯成功")
    void update_withIsLordTrue_returns200() throws Exception {
        PlanetPositionDto req    = makeDto(null, "金星", true);
        PlanetPositionDto updated = makeDto(5,   "金星", true);
        when(planetService.update(eq(1), eq(5), any(PlanetPositionDto.class))).thenReturn(updated);

        mockMvc.perform(put("/api/clients/1/planets/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.planet").value("金星"))
                .andExpect(jsonPath("$.isLord").value(true));
    }

    @Test
    @DisplayName("PUT /api/clients/1/planets/5 — 200 isLord=false 取消命主星")
    void update_withIsLordFalse_returns200() throws Exception {
        PlanetPositionDto req     = makeDto(null, "金星", false);
        PlanetPositionDto updated = makeDto(5,    "金星", false);
        when(planetService.update(eq(1), eq(5), any(PlanetPositionDto.class))).thenReturn(updated);

        mockMvc.perform(put("/api/clients/1/planets/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isLord").value(false));
    }

    @Test
    @DisplayName("PUT /api/clients/1/planets/999 — 404 行星不存在")
    void update_planetNotFound_returns404() throws Exception {
        when(planetService.update(eq(1), eq(999), any(PlanetPositionDto.class)))
                .thenThrow(new ResourceNotFoundException("PlanetPosition", 999));

        mockMvc.perform(put("/api/clients/1/planets/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(makeDto(null, "太陽", false))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/clients/999/planets/5 — 404 client 不存在")
    void update_clientNotFound_returns404() throws Exception {
        when(planetService.update(eq(999), eq(5), any(PlanetPositionDto.class)))
                .thenThrow(new ResourceNotFoundException("Client", 999));

        mockMvc.perform(put("/api/clients/999/planets/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(makeDto(null, "太陽", false))))
                .andExpect(status().isNotFound());
    }

    // ────────────────────────────────────────────────────
    // helper
    // ────────────────────────────────────────────────────

    /** id 帶入 Integer，允許 null（新增時） */
    private PlanetPositionDto makeDto(Integer id, String planet, Boolean isLord) {
        PlanetPositionDto dto = new PlanetPositionDto();
        dto.setId(id);
        dto.setPlanet(planet);
        dto.setSign("獅子座");
        dto.setDegreeNum((short) 17);
        dto.setMinuteNum((short) 28);
        dto.setHouse(10);
        dto.setIsLord(isLord);
        return dto;
    }

    /** id=null 簡化版（GET mock 用） */
    private PlanetPositionDto makeDto(int id, String planet, boolean isLord) {
        return makeDto((Integer) id, planet, isLord);
    }
}
