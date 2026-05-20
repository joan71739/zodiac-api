package com.project.zodiac_api.controller;

import com.project.zodiac_api.dto.ConsultationLogDto;
import com.project.zodiac_api.service.ConsultationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients/{clientId}/logs")
@RequiredArgsConstructor
public class ConsultationLogController {

    private final ConsultationLogService logService;

    // GET /api/clients/{clientId}/logs — 依 consultation_date 降序
    @GetMapping
    public ResponseEntity<List<ConsultationLogDto>> getAll(@PathVariable Integer clientId) {
        return ResponseEntity.ok(logService.getByClientId(clientId));
    }

    // POST /api/clients/{clientId}/logs — @Validated 觸發 @NotNull 驗證
    @PostMapping
    public ResponseEntity<ConsultationLogDto> create(
            @PathVariable Integer clientId,
            @Validated @RequestBody ConsultationLogDto dto) {
        return ResponseEntity.status(201).body(logService.create(clientId, dto));
    }

    // PUT /api/clients/{clientId}/logs/{lid} — @Validated 觸發 @NotNull 驗證
    @PutMapping("/{lid}")
    public ResponseEntity<ConsultationLogDto> update(
            @PathVariable Integer clientId,
            @PathVariable Integer lid,
            @Validated @RequestBody ConsultationLogDto dto) {
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
