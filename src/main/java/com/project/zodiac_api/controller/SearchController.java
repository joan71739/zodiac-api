package com.project.zodiac_api.controller;

import com.project.zodiac_api.dto.ClientResponseDto;
import com.project.zodiac_api.model.PlanetPosition;
import com.project.zodiac_api.repository.ClientRepository;
import com.project.zodiac_api.repository.PlanetPositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final PlanetPositionRepository planetRepo;
    private final ClientRepository clientRepo;

    /**
     * GET /api/search?planet=太陽&sign=獅子座&degreeFrom=10&degreeTo=20&house=10
     * planet、sign 必填；其餘選填
     */
    @GetMapping
    public ResponseEntity<List<ClientResponseDto>> search(
            @RequestParam String planet,
            @RequestParam String sign,
            @RequestParam(required = false) Short degreeFrom,
            @RequestParam(required = false) Short degreeTo,
            @RequestParam(required = false) Integer house) {

        if (planet == null || planet.isBlank() || sign == null || sign.isBlank()) {
            throw new IllegalArgumentException("planet 與 sign 為必填參數");
        }

        List<PlanetPosition> matches = planetRepo.search(planet, sign, degreeFrom, degreeTo, house);

        // 從符合的行星位置取出不重複的 clientId，再查客戶資料
        List<Integer> clientIds = matches.stream()
                .map(PlanetPosition::getClientId)
                .distinct()
                .toList();

        List<ClientResponseDto> clients = clientRepo.findAllById(clientIds).stream()
                .map(ClientResponseDto::from)
                .toList();

        return ResponseEntity.ok(clients);
    }
}
