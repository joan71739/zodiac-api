package com.project.zodiac_api.controller;

import com.project.zodiac_api.dto.ClientListDto;
import com.project.zodiac_api.dto.ClientRequestDto;
import com.project.zodiac_api.dto.ClientResponseDto;
import com.project.zodiac_api.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    // GET /api/clients — 列表，不含 ASC/MC
    @GetMapping
    public ResponseEntity<List<ClientListDto>> getAll() {
        return ResponseEntity.ok(clientService.getAll());
    }

    // GET /api/clients/{id} — 單筆，含 ASC/MC
    @GetMapping("/{id}")
    public ResponseEntity<ClientResponseDto> getOne(@PathVariable Integer id) {
        return ResponseEntity.ok(clientService.getById(id));
    }

    // POST /api/clients
    @PostMapping
    public ResponseEntity<ClientResponseDto> create(@Valid @RequestBody ClientRequestDto req) {
        return ResponseEntity.status(201).body(clientService.create(req));
    }

    // PUT /api/clients/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ClientResponseDto> update(
            @PathVariable Integer id,
            @Valid @RequestBody ClientRequestDto req) {
        return ResponseEntity.ok(clientService.update(id, req));
    }

    // DELETE /api/clients/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        clientService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // POST /api/clients/{id}/chart-image
    @PostMapping("/{id}/chart-image")
    public ResponseEntity<Void> uploadChartImage(
            @PathVariable Integer id,
            @RequestParam("file") MultipartFile file) throws IOException {
        clientService.uploadChartImage(id, file);
        return ResponseEntity.ok().build();
    }

    // GET /api/clients/{id}/chart-image
    @GetMapping("/{id}/chart-image")
    public ResponseEntity<Resource> getChartImage(@PathVariable Integer id) throws IOException {
        Resource resource = clientService.loadChartImage(id);
        String contentType = clientService.detectContentType(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(resource);
    }
}
