package com.project.zodiac_api.controller;

import com.project.zodiac_api.dto.ReferencePanelDto;
import com.project.zodiac_api.service.ReferencePanelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 解析參考面板 API
 *
 * GET /api/clients/{clientId}/reference-panel
 *
 * 回傳結構：
 *   ascSection    → 上升星座 × 各宮星座特性
 *   planetSections → 個人行星（太陽/月亮/水星/金星/火星）三層 element_notes
 */
@RestController
@RequestMapping("/api/clients/{clientId}")
@RequiredArgsConstructor
public class ReferencePanelController {

    private final ReferencePanelService panelService;

    @GetMapping("/reference-panel")
    public ResponseEntity<ReferencePanelDto> getPanel(@PathVariable Integer clientId) {
        return ResponseEntity.ok(panelService.buildPanel(clientId));
    }
}
