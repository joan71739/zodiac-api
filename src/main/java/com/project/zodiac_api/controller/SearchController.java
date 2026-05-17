package com.project.zodiac_api.controller;

import com.project.zodiac_api.dto.ClientResponseDto;
import com.project.zodiac_api.model.Client;
import com.project.zodiac_api.model.PlanetPosition;
import com.project.zodiac_api.repository.ClientRepository;
import com.project.zodiac_api.repository.PlanetPositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        // FIX #1: planet / sign 由 @RequestParam required=true 保證非 null，
        //         在此僅以 isBlank() 攔截前端未選擇時送出的空字串。
        //         原本 planet == null 的判斷永遠不會成立，已移除。
        if (planet.isBlank() || sign.isBlank()) {
            throw new IllegalArgumentException("planet 與 sign 為必填參數");
        }

        List<PlanetPosition> matches = planetRepo.search(planet, sign, degreeFrom, degreeTo, house);

        // 從符合的行星位置取出不重複的 clientId，保留 JPQL 回傳的自然順序
        List<Integer> clientIds = matches.stream()
                .map(PlanetPosition::getClientId)
                .distinct()
                .toList();

        // FIX #2: JpaRepository.findAllById() 底層為 WHERE id IN (...)，
        //         PostgreSQL 不保證回傳順序與傳入的 clientIds 清單一致。
        //         改用 Map 重建，依 clientIds（JPQL 結果順序）排列客戶列表。
        //         containsKey filter 同時可安全跳過孤兒 clientId（DB 中已被刪除的記錄）。
        Map<Integer, Client> clientMap = clientRepo.findAllById(clientIds).stream()
                .collect(Collectors.toMap(Client::getId, c -> c));

        List<ClientResponseDto> clients = clientIds.stream()
                .filter(clientMap::containsKey)
                .map(id -> ClientResponseDto.from(clientMap.get(id)))
                .toList();

        return ResponseEntity.ok(clients);
    }
}