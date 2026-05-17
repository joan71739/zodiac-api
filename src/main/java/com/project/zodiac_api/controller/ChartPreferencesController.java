package com.project.zodiac_api.controller;

import com.project.zodiac_api.dto.ChartPreferencesDTO;
import com.project.zodiac_api.service.ChartPreferencesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 全域星盤設定 API。
 *
 * 路由說明（與 ChartController 共享 /api/chart 前綴，無衝突）：
 *   ChartController      → GET /api/chart/{clientId}/data   (4 段)
 *                        → GET /api/chart/synastry          (3 段, literal)
 *   ChartPreferencesController → GET/PUT /api/chart/preferences  (3 段, literal)
 *                              → POST /api/chart/preferences/reset (4 段, literal)
 *
 * Spring MVC 優先匹配 literal 路徑（不含 path variable），無路由衝突。
 */
@RestController
@RequestMapping("/api/chart/preferences")
@RequiredArgsConstructor
public class ChartPreferencesController {

    private final ChartPreferencesService prefsService;

    /** GET /api/chart/preferences — 取得目前全域設定 */
    @GetMapping
    public ResponseEntity<ChartPreferencesDTO> get() {
        return ResponseEntity.ok(prefsService.get());
    }

    /** PUT /api/chart/preferences — 儲存設定 */
    @PutMapping
    public ResponseEntity<ChartPreferencesDTO> save(@RequestBody ChartPreferencesDTO dto) {
        return ResponseEntity.ok(prefsService.save(dto));
    }

    /** POST /api/chart/preferences/reset — 還原所有欄位為預設值 */
    @PostMapping("/reset")
    public ResponseEntity<ChartPreferencesDTO> reset() {
        return ResponseEntity.ok(prefsService.reset());
    }
}
