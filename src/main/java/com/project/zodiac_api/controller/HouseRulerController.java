package com.project.zodiac_api.controller;

import com.project.zodiac_api.dto.HouseRulerDto;
import com.project.zodiac_api.service.HouseRulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients/{clientId}/houses")
@RequiredArgsConstructor
public class HouseRulerController {

    private final HouseRulerService houseService;

    // GET /api/clients/{clientId}/houses
    @GetMapping
    public ResponseEntity<List<HouseRulerDto>> getAll(@PathVariable Integer clientId) {
        return ResponseEntity.ok(houseService.getByClientId(clientId));
    }

    // POST /api/clients/{clientId}/houses  （初次整批建立 12 筆）
    @PostMapping
    public ResponseEntity<List<HouseRulerDto>> createBatch(
            @PathVariable Integer clientId,
            @RequestBody List<HouseRulerDto> dtos) {
        return ResponseEntity.status(201).body(houseService.createBatch(clientId, dtos));
    }

    // PUT /api/clients/{clientId}/houses/{hid}  （單筆編輯）
    @PutMapping("/{hid}")
    public ResponseEntity<HouseRulerDto> update(
            @PathVariable Integer clientId,
            @PathVariable Integer hid,
            @RequestBody HouseRulerDto dto) {
        return ResponseEntity.ok(houseService.update(clientId, hid, dto));
    }
}
