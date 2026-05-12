package com.project.zodiac_api.service;

import com.project.zodiac_api.dto.AspectDto;
import com.project.zodiac_api.exception.ResourceNotFoundException;
import com.project.zodiac_api.model.Aspect;
import com.project.zodiac_api.repository.AspectRepository;
import com.project.zodiac_api.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AspectService {

    private final AspectRepository aspectRepo;
    private final ClientRepository clientRepo;

    // GET
    public List<AspectDto> getByClientId(Integer clientId) {
        ensureClientExists(clientId);
        return aspectRepo.findByClientId(clientId).stream()
                .map(AspectDto::from)
                .toList();
    }

    // POST — 新增單筆
    public AspectDto create(Integer clientId, AspectDto dto) {
        ensureClientExists(clientId);
        Aspect a = new Aspect();
        a.setClientId(clientId);
        mapDto(dto, a);
        return AspectDto.from(aspectRepo.save(a));
    }

    // PUT — 編輯單筆
    public AspectDto update(Integer clientId, Integer aid, AspectDto dto) {
        ensureClientExists(clientId);
        Aspect a = aspectRepo.findByClientIdAndId(clientId, aid)
                .orElseThrow(() -> new ResourceNotFoundException("Aspect", aid));
        mapDto(dto, a);
        return AspectDto.from(aspectRepo.save(a));
    }

    // DELETE
    public void delete(Integer clientId, Integer aid) {
        ensureClientExists(clientId);
        Aspect a = aspectRepo.findByClientIdAndId(clientId, aid)
                .orElseThrow(() -> new ResourceNotFoundException("Aspect", aid));
        aspectRepo.delete(a);
    }

    // ── helper ──────────────────────────────────────────

    private void ensureClientExists(Integer clientId) {
        if (!clientRepo.existsById(clientId)) {
            throw new ResourceNotFoundException("Client", clientId);
        }
    }

    private void mapDto(AspectDto dto, Aspect a) {
        a.setPlanet1(dto.getPlanet1());
        a.setAspectType(dto.getAspectType());
        a.setPlanet2(dto.getPlanet2());
        a.setOrb(dto.getOrb());
        a.setNotes(dto.getNotes());
    }
}
