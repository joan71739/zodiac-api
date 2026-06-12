package com.project.zodiac_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.zodiac_api.dto.ElementNoteDto;
import com.project.zodiac_api.dto.TransitNoteDto;
import com.project.zodiac_api.model.ElementNote;
import com.project.zodiac_api.model.TransitNote;
import com.project.zodiac_api.repository.ElementNoteRepository;
import com.project.zodiac_api.repository.TransitNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * 知識庫匯入 API
 *
 * POST /api/import/element-notes  — 匯入元素解析（upsert by id）
 * POST /api/import/transit-notes  — 匯入行運解析（upsert by id）
 *
 * upsert 規則：
 *   - JSON 內有 id → 找到對應 entity 就更新 title/content/tag/topic
 *   - JSON 內無 id 或找不到 id → 視為新增，sort_order 自動遞增
 *
 * 回傳：{ inserted: N, updated: N, skipped: N }
 */
@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class ImportController {

    private final ElementNoteRepository elementNoteRepo;
    private final TransitNoteRepository transitNoteRepo;
    private final ObjectMapper objectMapper;

    // ── 元素解析匯入 ────────────────────────────────────────────────────────

    /**
     * POST /api/import/element-notes
     * Content-Type: multipart/form-data
     * file: export_element_signs.json 或 export_element_planets.json
     */
    @PostMapping("/element-notes")
    public ResponseEntity<Map<String, Integer>> importElementNotes(
            @RequestParam("file") MultipartFile file) throws Exception {

        List<ElementNoteDto> dtos = Arrays.asList(
                objectMapper.readValue(file.getBytes(), ElementNoteDto[].class));

        int inserted = 0, updated = 0, skipped = 0;

        for (ElementNoteDto dto : dtos) {
            // 基本驗證：sign_key 必填
            if (dto.getSignKey() == null || dto.getSignKey().isBlank()) {
                skipped++;
                continue;
            }

            if (dto.getId() != null) {
                // 有 id → 嘗試更新
                Optional<ElementNote> opt = elementNoteRepo.findById(dto.getId());
                if (opt.isPresent()) {
                    ElementNote n = opt.get();
                    n.setTitle(dto.getTitle());
                    n.setContent(dto.getContent());
                    n.setTag(dto.getTag());
                    n.setTopic(dto.getTopic());
                    elementNoteRepo.save(n);
                    updated++;
                    continue;
                }
                // id 找不到 → 走新增流程
            }

            // 新增
            Integer maxOrder = elementNoteRepo.findMaxSortOrder(
                    dto.getSignKey(), dto.getPlanetKey(), dto.getHouseKey());

            ElementNote n = new ElementNote();
            n.setSignKey(dto.getSignKey());
            n.setPlanetKey(dto.getPlanetKey());
            n.setHouseKey(dto.getHouseKey());
            n.setTitle(dto.getTitle());
            n.setContent(dto.getContent());
            n.setTag(dto.getTag());
            n.setTopic(dto.getTopic());
            n.setSortOrder(maxOrder + 1);
            elementNoteRepo.save(n);
            inserted++;
        }

        return ResponseEntity.ok(Map.of(
                "inserted", inserted,
                "updated",  updated,
                "skipped",  skipped
        ));
    }

    // ── 行運解析匯入 ────────────────────────────────────────────────────────

    /**
     * POST /api/import/transit-notes
     * Content-Type: multipart/form-data
     * file: export_transit_notes.json
     */
    @PostMapping("/transit-notes")
    public ResponseEntity<Map<String, Integer>> importTransitNotes(
            @RequestParam("file") MultipartFile file) throws Exception {

        List<TransitNoteDto> dtos = Arrays.asList(
                objectMapper.readValue(file.getBytes(), TransitNoteDto[].class));

        int inserted = 0, updated = 0, skipped = 0;

        for (TransitNoteDto dto : dtos) {
            // 基本驗證：transit_planet 必填
            if (dto.getTransitPlanet() == null || dto.getTransitPlanet().isBlank()) {
                skipped++;
                continue;
            }

            if (dto.getId() != null) {
                // 有 id → 嘗試更新
                Optional<TransitNote> opt = transitNoteRepo.findById(dto.getId());
                if (opt.isPresent()) {
                    TransitNote n = opt.get();
                    n.setTitle(dto.getTitle());
                    n.setContent(dto.getContent());
                    n.setTag(dto.getTag());
                    n.setTopic(dto.getTopic());
                    transitNoteRepo.save(n);
                    updated++;
                    continue;
                }
                // id 找不到 → 走新增流程
            }

            // 新增
            Integer maxOrder = transitNoteRepo.findMaxSortOrder(
                    dto.getTransitPlanet(),
                    dto.getAspectType(),
                    dto.getNatalPlanet(),
                    dto.getTransitHouse());

            TransitNote n = new TransitNote();
            n.setTransitPlanet(dto.getTransitPlanet());
            n.setAspectType(dto.getAspectType());
            n.setNatalPlanet(dto.getNatalPlanet());
            n.setTransitHouse(dto.getTransitHouse());
            n.setTitle(dto.getTitle());
            n.setContent(dto.getContent());
            n.setTag(dto.getTag());
            n.setTopic(dto.getTopic());
            n.setSortOrder(maxOrder + 1);
            transitNoteRepo.save(n);
            inserted++;
        }

        return ResponseEntity.ok(Map.of(
                "inserted", inserted,
                "updated",  updated,
                "skipped",  skipped
        ));
    }
}
