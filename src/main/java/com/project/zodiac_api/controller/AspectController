package com.project.zodiac_api.controller;

import com.project.zodiac_api.dto.AspectDto;
import com.project.zodiac_api.service.AspectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients/{clientId}/aspects")
@RequiredArgsConstructor
public class AspectController {

    private final AspectService aspectService;

    // GET /api/clients/{clientId}/aspects
    @GetMapping
    public ResponseEntity<List<AspectDto>> getAll(@PathVariable Integer clientId) {
        return ResponseEntity.ok(aspectService.getByClientId(clientId));
    }

    // POST /api/clients/{clientId}/aspects
    @PostMapping
    public ResponseEntity<AspectDto> create(
            @PathVariable Integer clientId,
            @RequestBody AspectDto dto) {
        return ResponseEntity.status(201).body(aspectService.create(clientId, dto));
    }

    // PUT /api/clients/{clientId}/aspects/{aid}
    @PutMapping("/{aid}")
    public ResponseEntity<AspectDto> update(
            @PathVariable Integer clientId,
            @PathVariable Integer aid,
            @RequestBody AspectDto dto) {
        return ResponseEntity.ok(aspectService.update(clientId, aid, dto));
    }

    // DELETE /api/clients/{clientId}/aspects/{aid}
    @DeleteMapping("/{aid}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer clientId,
            @PathVariable Integer aid) {
        aspectService.delete(clientId, aid);
        return ResponseEntity.noContent().build();
    }
}
