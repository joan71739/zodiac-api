package com.project.zodiac_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.zodiac_api.dto.AspectDto;
import com.project.zodiac_api.exception.ResourceNotFoundException;
import com.project.zodiac_api.service.AspectService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AspectController.class)
@DisplayName("AspectController MockMvc 測試")
class AspectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AspectService aspectService;

    // ── GET ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/clients/1/aspects — 200 回傳相位列表")
    void getAll_returns200() throws Exception {
        AspectDto dto = makeDto(1, "Q", "q", "E", 2.0);  // 太陽 合相 水星
        when(aspectService.getByClientId(1)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/clients/1/aspects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].planet1").value("Q"))
                .andExpect(jsonPath("$[0].aspectType").value("q"))
                .andExpect(jsonPath("$[0].planet2").value("E"));
    }

    @Test
    @DisplayName("GET /api/clients/999/aspects — 404 客戶不存在")
    void getAll_clientNotFound_returns404() throws Exception {
        when(aspectService.getByClientId(999))
                .thenThrow(new ResourceNotFoundException("Client", 999));

        mockMvc.perform(get("/api/clients/999/aspects"))
                .andExpect(status().isNotFound());
    }

    // ── POST ────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/clients/1/aspects — 201 新增成功")
    void create_returns201() throws Exception {
        AspectDto req = new AspectDto();
        req.setPlanet1("Q");        // 太陽
        req.setAspectType("e");     // 三分相
        req.setPlanet2("Y");        // 木星
        req.setOrb(BigDecimal.valueOf(3.5));
        req.setNotes("吉相");

        AspectDto saved = makeDto(10, "Q", "e", "Y", 3.5);
        when(aspectService.create(eq(1), any(AspectDto.class))).thenReturn(saved);

        mockMvc.perform(post("/api/clients/1/aspects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.aspectType").value("e"));
    }

    // ── PUT ─────────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/clients/1/aspects/10 — 200 編輯成功")
    void update_returns200() throws Exception {
        AspectDto req = new AspectDto();
        req.setPlanet1("Q");        // 太陽
        req.setAspectType("r");     // 四分相
        req.setPlanet2("U");        // 土星
        req.setOrb(BigDecimal.valueOf(1.5));
        req.setNotes("凶相");

        AspectDto updated = makeDto(10, "Q", "r", "U", 1.5);
        when(aspectService.update(eq(1), eq(10), any(AspectDto.class))).thenReturn(updated);

        mockMvc.perform(put("/api/clients/1/aspects/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aspectType").value("r"))
                .andExpect(jsonPath("$.planet2").value("U"));
    }

    @Test
    @DisplayName("PUT /api/clients/1/aspects/999 — 404 相位不存在")
    void update_notFound_returns404() throws Exception {
        AspectDto req = new AspectDto();
        req.setPlanet1("Q");        // 太陽
        req.setAspectType("r");     // 四分相
        req.setPlanet2("U");        // 土星

        when(aspectService.update(eq(1), eq(999), any(AspectDto.class)))
                .thenThrow(new ResourceNotFoundException("Aspect", 999));

        mockMvc.perform(put("/api/clients/1/aspects/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    // ── DELETE ──────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/clients/1/aspects/10 — 204 刪除成功")
    void delete_returns204() throws Exception {
        doNothing().when(aspectService).delete(1, 10);

        mockMvc.perform(delete("/api/clients/1/aspects/10"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/clients/1/aspects/999 — 404 相位不存在")
    void delete_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Aspect", 999))
                .when(aspectService).delete(1, 999);

        mockMvc.perform(delete("/api/clients/1/aspects/999"))
                .andExpect(status().isNotFound());
    }

    // ── helper ──────────────────────────────────────────

    private AspectDto makeDto(int id, String p1, String type, String p2, double orb) {
        AspectDto dto = new AspectDto();
        dto.setId(id);
        dto.setPlanet1(p1);
        dto.setAspectType(type);
        dto.setPlanet2(p2);
        dto.setOrb(BigDecimal.valueOf(orb));
        return dto;
    }
}
