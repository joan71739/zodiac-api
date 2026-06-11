package com.project.zodiac_api.controller;

import com.project.zodiac_api.dto.TransitNoteDto;
import com.project.zodiac_api.service.TransitNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 行運解析 API
 *
 * 查詢組合說明：
 *
 *   情境一：行運星 × 相位 × 本命星
 *     transitPlanet='Y', aspectType='q', natalPlanet='Q', transitHouse=null
 *     → 木星合相本命太陽（詳細資料頁籤）
 *
 *     transitPlanet='Y', aspectType='q', natalPlanet='Q', transitHouse=1
 *     → 木星合相本命太陽，過境一宮頁籤
 *
 *   情境二：行運星 × 過境宮位
 *     transitPlanet='Y', aspectType=null, natalPlanet=null, transitHouse=2
 *     → 木星過境二宮
 *
 * 端點：
 *   GET    /api/transit-notes?transitPlanet=Y&aspectType=q&natalPlanet=Q&transitHouse=
 *   POST   /api/transit-notes?transitPlanet=Y&aspectType=q&natalPlanet=Q&transitHouse=
 *   PUT    /api/transit-notes/{id}
 *   DELETE /api/transit-notes/{id}
 */
@RestController
@RequestMapping("/api/transit-notes")
@RequiredArgsConstructor
public class TransitNoteController {

    private final TransitNoteService noteService;

    // ── GET ──────────────────────────────────────────────────────────────────

    /**
     * GET /api/transit-notes
     *   ?transitPlanet=Y&aspectType=q&natalPlanet=Q&transitHouse=1
     *
     * aspectType、natalPlanet、transitHouse 選填
     * 回傳依 sort_order DESC，最新在最上
     */
    @GetMapping
    public ResponseEntity<List<TransitNoteDto>> getAll(
            @RequestParam               String transitPlanet,
            @RequestParam(required = false) String aspectType,
            @RequestParam(required = false) String natalPlanet,
            @RequestParam(required = false) Short  transitHouse) {

        return ResponseEntity.ok(
                noteService.getByKeys(transitPlanet, aspectType, natalPlanet, transitHouse));
    }

    // ── POST ─────────────────────────────────────────────────────────────────

    /**
     * POST /api/transit-notes
     *   ?transitPlanet=Y&aspectType=q&natalPlanet=Q&transitHouse=
     *
     * body: { title, content, tag, topic }
     * sort_order 後端自動遞增，不需傳入
     */
    @PostMapping
    public ResponseEntity<TransitNoteDto> create(
            @RequestParam               String transitPlanet,
            @RequestParam(required = false) String aspectType,
            @RequestParam(required = false) String natalPlanet,
            @RequestParam(required = false) Short  transitHouse,
            @RequestBody TransitNoteDto dto) {

        return ResponseEntity.status(201).body(
                noteService.create(transitPlanet, aspectType, natalPlanet, transitHouse, dto));
    }

    // ── PUT ──────────────────────────────────────────────────────────────────

    /**
     * PUT /api/transit-notes/{id}
     *
     * body: { title, content, tag, topic }
     * 鍵值欄位（transit_planet / aspect_type / natal_planet / transit_house / sort_order）不異動
     */
    @PutMapping("/{id}")
    public ResponseEntity<TransitNoteDto> update(
            @PathVariable Integer id,
            @RequestBody  TransitNoteDto dto) {

        return ResponseEntity.ok(noteService.update(id, dto));
    }

    // ── DELETE ───────────────────────────────────────────────────────────────

    /**
     * DELETE /api/transit-notes/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        noteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
