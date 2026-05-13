package com.project.zodiac_api.controller;

import com.project.zodiac_api.dto.PlanetPositionDto;
import com.project.zodiac_api.service.PlanetPositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients/{clientId}/planets")
@RequiredArgsConstructor
public class PlanetPositionController {

    private final PlanetPositionService planetService;

    // GET /api/clients/{clientId}/planets
    @GetMapping
    public ResponseEntity<List<PlanetPositionDto>> getAll(@PathVariable Integer clientId) {
        return ResponseEntity.ok(planetService.getByClientId(clientId));
    }

    // POST /api/clients/{clientId}/planets  （初次整批建立）
    @PostMapping
    public ResponseEntity<List<PlanetPositionDto>> createBatch(
            @PathVariable Integer clientId,
            @RequestBody List<PlanetPositionDto> dtos) {
        return ResponseEntity.status(201).body(planetService.createBatch(clientId, dtos));
    }

    // PUT /api/clients/{clientId}/planets/{pid}  （單筆編輯）
    @PutMapping("/{pid}")
    public ResponseEntity<PlanetPositionDto> update(
            @PathVariable Integer clientId,
            @PathVariable Integer pid,
            @RequestBody PlanetPositionDto dto) {
        return ResponseEntity.ok(planetService.update(clientId, pid, dto));
    }
}
