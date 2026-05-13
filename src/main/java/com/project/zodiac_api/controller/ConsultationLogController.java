package com.project.zodiac_api.controller;

import com.project.zodiac_api.dto.ConsultationLogDto;
import com.project.zodiac_api.service.ConsultationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients/{clientId}/logs")
@RequiredArgsConstructor
public class ConsultationLogController {

    private final ConsultationLogService logService;

    // GET /api/clients/{clientId}/logs
    @GetMapping
    public ResponseEntity<List<ConsultationLogDto>> getAll(@PathVariable Integer clientId) {
        return ResponseEntity.ok(logService.getByClientId(clientId));
    }

    // POST /api/clients/{clientId}/logs
    @PostMapping
    public ResponseEntity<ConsultationLogDto> create(
            @PathVariable Integer clientId,
            @RequestBody ConsultationLogDto dto) {
        return ResponseEntity.status(201).body(logService.create(clientId, dto));
    }

    // PUT /api/clients/{clientId}/logs/{lid}
    @PutMapping("/{lid}")
    public ResponseEntity<ConsultationLogDto> update(
            @PathVariable Integer clientId,
            @PathVariable Integer lid,
            @RequestBody ConsultationLogDto dto) {
        return ResponseEntity.ok(logService.update(clientId, lid, dto));
    }

    // DELETE /api/clients/{clientId}/logs/{lid}
    @DeleteMapping("/{lid}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer clientId,
            @PathVariable Integer lid) {
        logService.delete(clientId, lid);
        return ResponseEntity.noContent().build();
    }
}
