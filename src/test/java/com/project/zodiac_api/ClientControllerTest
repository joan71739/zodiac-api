package com.project.zodiac_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.zodiac_api.dto.ClientRequestDto;
import com.project.zodiac_api.dto.ClientResponseDto;
import com.project.zodiac_api.exception.ResourceNotFoundException;
import com.project.zodiac_api.service.ClientService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientController.class)
@DisplayName("ClientController MockMvc 測試")
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClientService clientService;

    // ── GET /api/clients ─────────────────────────────────

    @Test
    @DisplayName("GET /api/clients — 200 回傳客戶列表")
    void getAll_returns200() throws Exception {
        ClientResponseDto dto = makeResponseDto(1, "王小明", "台北");
        when(clientService.getAll()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("王小明"))
                .andExpect(jsonPath("$[0].birthPlace").value("台北"));
    }

    // ── GET /api/clients/{id} ────────────────────────────

    @Test
    @DisplayName("GET /api/clients/1 — 200 回傳單一客戶")
    void getOne_exists_returns200() throws Exception {
        when(clientService.getById(1)).thenReturn(makeResponseDto(1, "王小明", "台北"));

        mockMvc.perform(get("/api/clients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("王小明"));
    }

    @Test
    @DisplayName("GET /api/clients/999 — 404 客戶不存在")
    void getOne_notFound_returns404() throws Exception {
        when(clientService.getById(999))
                .thenThrow(new ResourceNotFoundException("Client", 999));

        mockMvc.perform(get("/api/clients/999"))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/clients ────────────────────────────────

    @Test
    @DisplayName("POST /api/clients — 201 新增成功")
    void create_validBody_returns201() throws Exception {
        ClientRequestDto req = new ClientRequestDto();
        req.setName("林小美");
        req.setBirthDate(LocalDate.of(1995, 3, 15));
        req.setBirthTime(LocalTime.of(12, 0));
        req.setBirthPlace("高雄");

        ClientResponseDto resp = makeResponseDto(2, "林小美", "高雄");
        when(clientService.create(any(ClientRequestDto.class))).thenReturn(resp);

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("林小美"));
    }

    @Test
    @DisplayName("POST /api/clients — 400 name 為空")
    void create_missingName_returns400() throws Exception {
        ClientRequestDto req = new ClientRequestDto();
        req.setName("");    // 空字串，@NotBlank 驗證失敗

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ── PUT /api/clients/{id} ────────────────────────────

    @Test
    @DisplayName("PUT /api/clients/1 — 200 編輯成功")
    void update_exists_returns200() throws Exception {
        ClientRequestDto req = new ClientRequestDto();
        req.setName("王大明");
        req.setBirthDate(LocalDate.of(1993, 8, 10));
        req.setBirthTime(LocalTime.of(8, 30));
        req.setBirthPlace("新北");

        ClientResponseDto resp = makeResponseDto(1, "王大明", "新北");
        when(clientService.update(eq(1), any(ClientRequestDto.class))).thenReturn(resp);

        mockMvc.perform(put("/api/clients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("王大明"));
    }

    @Test
    @DisplayName("PUT /api/clients/999 — 404 客戶不存在")
    void update_notFound_returns404() throws Exception {
        ClientRequestDto req = new ClientRequestDto();
        req.setName("不存在");

        when(clientService.update(eq(999), any(ClientRequestDto.class)))
                .thenThrow(new ResourceNotFoundException("Client", 999));

        mockMvc.perform(put("/api/clients/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /api/clients/{id} ─────────────────────────

    @Test
    @DisplayName("DELETE /api/clients/1 — 204 刪除成功")
    void delete_exists_returns204() throws Exception {
        doNothing().when(clientService).delete(1);

        mockMvc.perform(delete("/api/clients/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/clients/999 — 404 客戶不存在")
    void delete_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Client", 999)).when(clientService).delete(999);

        mockMvc.perform(delete("/api/clients/999"))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/clients/{id}/chart-image ───────────────

    @Test
    @DisplayName("POST /api/clients/1/chart-image — 200 上傳成功")
    void uploadChartImage_returns200() throws Exception {
        doNothing().when(clientService).uploadChartImage(eq(1), any());

        MockMultipartFile file = new MockMultipartFile(
                "file", "chart.jpg", "image/jpeg", "fake-image".getBytes());

        mockMvc.perform(multipart("/api/clients/1/chart-image").file(file))
                .andExpect(status().isOk());
    }

    // ── helper ──────────────────────────────────────────

    private ClientResponseDto makeResponseDto(int id, String name, String place) {
        ClientResponseDto dto = new ClientResponseDto();
        dto.setId(id);
        dto.setName(name);
        dto.setBirthDate(LocalDate.of(1993, 8, 10));
        dto.setBirthTime(LocalTime.of(8, 30));
        dto.setBirthPlace(place);
        return dto;
    }
}
