package com.project.zodiac_api.controller;

import com.project.zodiac_api.dto.AnalysisNoteDto;
import com.project.zodiac_api.service.AnalysisNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients/{clientId}/notes")
@RequiredArgsConstructor
public class AnalysisNoteController {

    private final AnalysisNoteService noteService;

    // GET /api/clients/{clientId}/notes
    @GetMapping
    public ResponseEntity<List<AnalysisNoteDto>> getAll(@PathVariable Integer clientId) {
        return ResponseEntity.ok(noteService.getByClientId(clientId));
    }

    // POST /api/clients/{clientId}/notes
    @PostMapping
    public ResponseEntity<AnalysisNoteDto> create(
            @PathVariable Integer clientId,
            @RequestBody AnalysisNoteDto dto) {
        return ResponseEntity.status(201).body(noteService.create(clientId, dto));
    }

    // PUT /api/clients/{clientId}/notes/{nid}
    @PutMapping("/{nid}")
    public ResponseEntity<AnalysisNoteDto> update(
            @PathVariable Integer clientId,
            @PathVariable Integer nid,
            @RequestBody AnalysisNoteDto dto) {
        return ResponseEntity.ok(noteService.update(clientId, nid, dto));
    }

    // DELETE /api/clients/{clientId}/notes/{nid}
    @DeleteMapping("/{nid}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer clientId,
            @PathVariable Integer nid) {
        noteService.delete(clientId, nid);
        return ResponseEntity.noContent().build();
    }
}
