package com.project.zodiac_api.service;

import com.project.zodiac_api.dto.PlanetPositionDto;
import com.project.zodiac_api.exception.ResourceNotFoundException;
import com.project.zodiac_api.model.PlanetPosition;
import com.project.zodiac_api.repository.ClientRepository;
import com.project.zodiac_api.repository.PlanetPositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanetPositionService {

    private final PlanetPositionRepository planetRepo;
    private final ClientRepository clientRepo;

    // GET — 依 id 升冪回傳，確保順序與 INSERT 順序一致
    public List<PlanetPositionDto> getByClientId(Integer clientId) {
        ensureClientExists(clientId);
        return planetRepo.findByClientIdOrderByIdAsc(clientId).stream()
                .map(PlanetPositionDto::from)
                .toList();
    }

    // POST — 初次整批建立；先刪舊資料防止重複 POST，再整批 saveAll
    @Transactional
    public List<PlanetPositionDto> createBatch(Integer clientId, List<PlanetPositionDto> dtos) {
        ensureClientExists(clientId);
        planetRepo.deleteByClientId(clientId);

        List<PlanetPosition> entities = dtos.stream().map(dto -> {
            PlanetPosition p = new PlanetPosition();
            p.setClientId(clientId);
            mapDto(dto, p);
            return p;
        }).toList();

        return planetRepo.saveAll(entities).stream()
                .map(PlanetPositionDto::from)
                .toList();
    }

    // PUT — 單筆編輯
    // 若 isLord=true，先以 clearLordByClientId 清除同 client 所有行星的旗標，
    // 再設定當前行星，確保同一客戶全局唯一性。
    // @Transactional 確保 clear + save 在同一 DB transaction 內完成。
    @Transactional
    public PlanetPositionDto update(Integer clientId, Integer pid, PlanetPositionDto dto) {
        ensureClientExists(clientId);
        PlanetPosition p = planetRepo.findById(pid)
                .filter(x -> x.getClientId().equals(clientId))
                .orElseThrow(() -> new ResourceNotFoundException("PlanetPosition", pid));

        if (Boolean.TRUE.equals(dto.getIsLord())) {
            planetRepo.clearLordByClientId(clientId);
        }

        mapDto(dto, p);
        return PlanetPositionDto.from(planetRepo.save(p));
    }

    // ── helper ──────────────────────────────────────────

    private void ensureClientExists(Integer clientId) {
        if (!clientRepo.existsById(clientId)) {
            throw new ResourceNotFoundException("Client", clientId);
        }
    }

    private void mapDto(PlanetPositionDto dto, PlanetPosition p) {
        p.setPlanet(dto.getPlanet());
        p.setSign(dto.getSign());
        p.setDegreeNum(dto.getDegreeNum());
        p.setMinuteNum(dto.getMinuteNum());
        p.setHouse(dto.getHouse());
        p.setNotes(dto.getNotes());
        p.setIsLord(dto.getIsLord() != null ? dto.getIsLord() : false);
    }
}
