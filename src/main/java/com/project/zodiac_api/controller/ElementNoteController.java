package com.project.zodiac_api.controller;

import com.project.zodiac_api.dto.ElementNoteDto;
import com.project.zodiac_api.service.ElementNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 元素解析 API
 *
 * 查詢組合說明：
 *   sign_key='a', planet_key=null, house_key=null  → 牡羊座  星座特性
 *   sign_key='a', planet_key=null, house_key=1     → 牡羊座  一宮
 *   sign_key='a', planet_key='Q',  house_key=null  → 太陽牡羊座  星座特性
 *   sign_key='a', planet_key='Q',  house_key=1     → 太陽牡羊座  一宮
 *
 * 端點：
 *   GET    /api/element-notes?signKey=a&planetKey=Q&houseKey=1
 *   POST   /api/element-notes?signKey=a&planetKey=Q&houseKey=1
 *   PUT    /api/element-notes/{id}
 *   DELETE /api/element-notes/{id}
 */
@RestController
@RequestMapping("/api/element-notes")
@RequiredArgsConstructor
public class ElementNoteController {

    private final ElementNoteService noteService;

    // ── GET ─────────────────────────────────────────────────────────────────

    /**
     * GET /api/element-notes?signKey=a&planetKey=Q&houseKey=1
     *
     * planetKey、houseKey 選填（null = 星座解析 / 星座特性頁籤）
     * 回傳依 sort_order DESC，最新在最上
     */
    @GetMapping
    public ResponseEntity<List<ElementNoteDto>> getAll(
            @RequestParam String signKey,
            @RequestParam(required = false) String planetKey,
            @RequestParam(required = false) Short  houseKey) {

        return ResponseEntity.ok(noteService.getByKeys(signKey, planetKey, houseKey));
    }

    // ── POST ─────────────────────────────────────────────────────────────────

    /**
     * POST /api/element-notes?signKey=a&planetKey=Q&houseKey=1
     *
     * body: { title, content, tag }
     * sort_order 後端自動遞增，不需傳入
     */
    @PostMapping
    public ResponseEntity<ElementNoteDto> create(
            @RequestParam String signKey,
            @RequestParam(required = false) String planetKey,
            @RequestParam(required = false) Short  houseKey,
            @RequestBody ElementNoteDto dto) {

        return ResponseEntity.status(201)
                .body(noteService.create(signKey, planetKey, houseKey, dto));
    }

    // ── PUT ──────────────────────────────────────────────────────────────────

    /**
     * PUT /api/element-notes/{id}
     *
     * body: { title, content, tag }
     * sign_key / planet_key / house_key / sort_order 不異動
     */
    @PutMapping("/{id}")
    public ResponseEntity<ElementNoteDto> update(
            @PathVariable Integer id,
            @RequestBody ElementNoteDto dto) {

        return ResponseEntity.ok(noteService.update(id, dto));
    }

    // ── DELETE ───────────────────────────────────────────────────────────────

    /**
     * DELETE /api/element-notes/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        noteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
