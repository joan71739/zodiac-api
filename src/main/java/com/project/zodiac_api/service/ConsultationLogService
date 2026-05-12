package com.project.zodiac_api.service;

import com.project.zodiac_api.dto.ConsultationLogDto;
import com.project.zodiac_api.exception.ResourceNotFoundException;
import com.project.zodiac_api.model.ConsultationLog;
import com.project.zodiac_api.repository.ClientRepository;
import com.project.zodiac_api.repository.ConsultationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultationLogService {

    private final ConsultationLogRepository logRepo;
    private final ClientRepository clientRepo;

    // GET — 依 consultation_date 降序
    public List<ConsultationLogDto> getByClientId(Integer clientId) {
        ensureClientExists(clientId);
        return logRepo.findByClientIdOrderByConsultationDateDesc(clientId).stream()
                .map(ConsultationLogDto::from)
                .toList();
    }

    // POST — 新增
    public ConsultationLogDto create(Integer clientId, ConsultationLogDto dto) {
        ensureClientExists(clientId);
        ConsultationLog log = new ConsultationLog();
        log.setClientId(clientId);
        mapDto(dto, log);
        return ConsultationLogDto.from(logRepo.save(log));
    }

    // PUT — 編輯
    public ConsultationLogDto update(Integer clientId, Integer lid, ConsultationLogDto dto) {
        ensureClientExists(clientId);
        ConsultationLog log = logRepo.findByClientIdAndId(clientId, lid)
                .orElseThrow(() -> new ResourceNotFoundException("ConsultationLog", lid));
        mapDto(dto, log);
        return ConsultationLogDto.from(logRepo.save(log));
    }

    // DELETE
    public void delete(Integer clientId, Integer lid) {
        ensureClientExists(clientId);
        ConsultationLog log = logRepo.findByClientIdAndId(clientId, lid)
                .orElseThrow(() -> new ResourceNotFoundException("ConsultationLog", lid));
        logRepo.delete(log);
    }

    // ── helper ──────────────────────────────────────────

    private void ensureClientExists(Integer clientId) {
        if (!clientRepo.existsById(clientId)) {
            throw new ResourceNotFoundException("Client", clientId);
        }
    }

    private void mapDto(ConsultationLogDto dto, ConsultationLog log) {
        log.setConsultationDate(dto.getConsultationDate());
        log.setNotes(dto.getNotes());
    }
}
