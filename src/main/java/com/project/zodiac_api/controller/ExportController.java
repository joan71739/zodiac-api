package com.project.zodiac_api.controller;

import com.project.zodiac_api.dto.*;
import com.project.zodiac_api.exception.ResourceNotFoundException;
import com.project.zodiac_api.model.*;
import com.project.zodiac_api.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
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
    // 以 id ASC 固定排序，確保每次匯出順序穩定可重現
    @GetMapping("/clients")
    public ResponseEntity<byte[]> exportClients() throws Exception {
        List<ClientResponseDto> data = clientRepo.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
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
        data.put("planets", planetPositionRepo.findByClientIdOrderByIdAsc(id).stream().map(PlanetPositionDto::from).toList());
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

        // planet / sign 由 @RequestParam 保證非 null，以 isBlank() 攔截空字串即可
        if (planet.isBlank() || sign.isBlank()) {
            throw new IllegalArgumentException("planet 與 sign 為必填參數");
        }

        // 以 List 保留搜尋結果的原始順序（行星命中順序）
        List<Integer> clientIds = planetPositionRepo.search(planet, sign, degreeFrom, degreeTo, house)
                .stream().map(PlanetPosition::getClientId).distinct().toList();

        // Map 僅作 O(1) 查找，最終順序由 clientIds List 決定；
        // containsKey filter 可安全跳過孤兒 clientId（行星存在但客戶已被刪除的邊界情況）
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
        // Content-Type 正確附加 charset=UTF-8（非 Content-Encoding，後者用於描述壓縮編碼）
        MediaType contentType = new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(bytes);
    }
}
