package com.project.zodiac_api.service;

import com.project.zodiac_api.dto.HouseRulerDto;
import com.project.zodiac_api.exception.ResourceNotFoundException;
import com.project.zodiac_api.model.HouseRuler;
import com.project.zodiac_api.repository.ClientRepository;
import com.project.zodiac_api.repository.HouseRulerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HouseRulerService {

    private final HouseRulerRepository houseRepo;
    private final ClientRepository     clientRepo;

    // GET — 依 houseNumber 排序回傳
    public List<HouseRulerDto> getByClientId(Integer clientId) {
        ensureClientExists(clientId);
        return houseRepo.findByClientIdOrderByHouseNumberAsc(clientId).stream()
                .map(HouseRulerDto::from)
                .toList();
    }

    // POST — 初次整批建立（12 筆）
    @Transactional
    public List<HouseRulerDto> createBatch(Integer clientId, List<HouseRulerDto> dtos) {
        ensureClientExists(clientId);
        // 清除舊資料（防止重複 POST）
        houseRepo.deleteByClientId(clientId);

        List<HouseRuler> entities = dtos.stream().map(dto -> {
            HouseRuler h = new HouseRuler();
            h.setClientId(clientId);
            mapDto(dto, h);
            return h;
        }).toList();

        return houseRepo.saveAll(entities).stream()
                .map(HouseRulerDto::from)
                .toList();
    }

    // PUT — 單筆編輯
    public HouseRulerDto update(Integer clientId, Integer hid, HouseRulerDto dto) {
        ensureClientExists(clientId);
        HouseRuler h = houseRepo.findByClientIdAndId(clientId, hid)
                .orElseThrow(() -> new ResourceNotFoundException("HouseRuler", hid));
        mapDto(dto, h);
        return HouseRulerDto.from(houseRepo.save(h));
    }

    // ── helper ──────────────────────────────────────────

    private void ensureClientExists(Integer clientId) {
        if (!clientRepo.existsById(clientId)) {
            throw new ResourceNotFoundException("Client", clientId);
        }
    }

    // v18：補上 houseSign
    private void mapDto(HouseRulerDto dto, HouseRuler h) {
        h.setHouseNumber(dto.getHouseNumber());
        h.setHouseSign(dto.getHouseSign());
        h.setRulingPlanet(dto.getRulingPlanet());
        h.setFliesToSign(dto.getFliesToSign());
        h.setFliesToHouse(dto.getFliesToHouse());
    }
}
