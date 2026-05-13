package com.project.zodiac_api.service;

import com.project.zodiac_api.dto.AnalysisNoteDto;
import com.project.zodiac_api.exception.ResourceNotFoundException;
import com.project.zodiac_api.model.AnalysisNote;
import com.project.zodiac_api.repository.AnalysisNoteRepository;
import com.project.zodiac_api.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalysisNoteService {

    private final AnalysisNoteRepository noteRepo;
    private final ClientRepository clientRepo;

    // GET — 依 sort_order 降序（v8：最新的在最上面）
    public List<AnalysisNoteDto> getByClientId(Integer clientId) {
        ensureClientExists(clientId);
        return noteRepo.findByClientIdOrderBySortOrderDesc(clientId).stream()
                .map(AnalysisNoteDto::from)
                .toList();
    }

    // POST — 新增（sort_order 後端自動遞增）
    public AnalysisNoteDto create(Integer clientId, AnalysisNoteDto dto) {
        ensureClientExists(clientId);
        Integer maxOrder = noteRepo.findMaxSortOrderByClientId(clientId);

        AnalysisNote n = new AnalysisNote();
        n.setClientId(clientId);
        n.setTitle(dto.getTitle());
        n.setContent(dto.getContent());
        n.setSortOrder(maxOrder + 1);
        return AnalysisNoteDto.from(noteRepo.save(n));
    }

    // PUT — 編輯
    public AnalysisNoteDto update(Integer clientId, Integer nid, AnalysisNoteDto dto) {
        ensureClientExists(clientId);
        AnalysisNote n = noteRepo.findByClientIdAndId(clientId, nid)
                .orElseThrow(() -> new ResourceNotFoundException("AnalysisNote", nid));
        n.setTitle(dto.getTitle());
        n.setContent(dto.getContent());
        return AnalysisNoteDto.from(noteRepo.save(n));
    }

    // DELETE
    public void delete(Integer clientId, Integer nid) {
        ensureClientExists(clientId);
        AnalysisNote n = noteRepo.findByClientIdAndId(clientId, nid)
                .orElseThrow(() -> new ResourceNotFoundException("AnalysisNote", nid));
        noteRepo.delete(n);
    }

    // ── helper ──────────────────────────────────────────

    private void ensureClientExists(Integer clientId) {
        if (!clientRepo.existsById(clientId)) {
            throw new ResourceNotFoundException("Client", clientId);
        }
    }
}
