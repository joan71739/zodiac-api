package com.project.zodiac_api.controller;

import com.project.zodiac_api.dto.*;
import com.project.zodiac_api.exception.ResourceNotFoundException;
import com.project.zodiac_api.model.*;
import com.project.zodiac_api.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final ClientRepository clientRepo;
    private final PlanetPositionRepository planetPositionRepo;
    private final HouseRulerRepository houseRepo;
    private final AspectRepository aspectRepo;
    private final AnalysisNoteRepository noteRepo;
    private final ConsultationLogRepository logRepo;
    private final ObjectMapper objectMapper;

    // GET /api/export/clients
    @GetMapping("/clients")
    public ResponseEntity<byte[]> exportClients() throws Exception {
        List<ClientResponseDto> data = clientRepo.findAll().stream()
                .map(ClientResponseDto::from)
                .toList();
        return buildResponse(data, "export_clients.json");
    }

    // GET /api/export/clients/{id}/chart
    @GetMapping("/clients/{id}/chart")
    public ResponseEntity<byte[]> exportChart(@PathVariable Integer id) throws Exception {
        Client client = clientRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", id));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("client",  ClientResponseDto.from(client));
        data.put("planets", planetPositionRepo.findByClientId(id).stream().map(PlanetPositionDto::from).toList());
        data.put("houses",  houseRepo.findByClientIdOrderByHouseNumberAsc(id).stream().map(HouseRulerDto::from).toList());
        data.put("aspects", aspectRepo.findByClientId(id).stream().map(AspectDto::from).toList());

        return buildResponse(data, "export_chart_" + id + ".json");
    }

    // GET /api/export/clients/{id}/notes
    @GetMapping("/clients/{id}/notes")
    public ResponseEntity<byte[]> exportNotes(@PathVariable Integer id) throws Exception {
        if (!clientRepo.existsById(id)) throw new ResourceNotFoundException("Client", id);

        List<AnalysisNoteDto> data = noteRepo.findByClientIdOrderBySortOrderDesc(id).stream()
                .map(AnalysisNoteDto::from)
                .toList();
        return buildResponse(data, "export_notes_" + id + ".json");
    }

    // GET /api/export/clients/{id}/logs
    @GetMapping("/clients/{id}/logs")
    public ResponseEntity<byte[]> exportLogs(@PathVariable Integer id) throws Exception {
        if (!clientRepo.existsById(id)) throw new ResourceNotFoundException("Client", id);

        List<ConsultationLogDto> data = logRepo.findByClientIdOrderByConsultationDateDesc(id).stream()
                .map(ConsultationLogDto::from)
                .toList();
        return buildResponse(data, "export_logs_" + id + ".json");
    }

    // GET /api/export/search?planet=xx&sign=xx&degreeFrom=xx&degreeTo=xx&house=xx
    @GetMapping("/search")
    public ResponseEntity<byte[]> exportSearch(
            @RequestParam String planet,
            @RequestParam String sign,
            @RequestParam(required = false) Short degreeFrom,
            @RequestParam(required = false) Short degreeTo,
            @RequestParam(required = false) Integer house) throws Exception {

        // FIX #1: 同 SearchController，planet/sign 非 null 由框架保證，
        //         僅需 isBlank() 攔截空字串。
        if (planet.isBlank() || sign.isBlank()) {
            throw new IllegalArgumentException("planet 與 sign 為必填參數");
        }

        List<Integer> clientIds = planetPositionRepo.search(planet, sign, degreeFrom, degreeTo, house)
                .stream().map(PlanetPosition::getClientId).distinct().toList();

        // FIX #2: 同 SearchController，Map 重建保證匯出資料順序與搜尋結果一致，
        //         containsKey filter 同時可安全跳過孤兒 clientId。
        Map<Integer, Client> clientMap = clientRepo.findAllById(clientIds).stream()
                .collect(Collectors.toMap(Client::getId, c -> c));

        List<ClientResponseDto> data = clientIds.stream()
                .filter(clientMap::containsKey)
                .map(id -> ClientResponseDto.from(clientMap.get(id)))
                .toList();

        return buildResponse(data, "export_search.json");
    }

    // ── helper ──────────────────────────────────────────

    private ResponseEntity<byte[]> buildResponse(Object data, String filename) throws Exception {
        byte[] bytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(data);
        // FIX：移除錯誤的 Content-Encoding: UTF-8（該 header 用於描述壓縮編碼，非字元編碼）
        //      改用 MediaType 正確附加 charset=UTF-8 至 Content-Type
        MediaType contentType = new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(bytes);
    }
}