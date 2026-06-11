package com.project.zodiac_api.service;

import com.project.zodiac_api.dto.AnalysisNoteDto;
import com.project.zodiac_api.exception.ResourceNotFoundException;
import com.project.zodiac_api.model.AnalysisNote;
import com.project.zodiac_api.repository.AnalysisNoteRepository;
import com.project.zodiac_api.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AnalysisNoteService {

    private final AnalysisNoteRepository noteRepo;
    private final ClientRepository       clientRepo;

    private static final Set<String> VALID_TOPICS = Set.of(
            "general", "career", "love", "wealth", "challenge"
    );

    // ── GET ──────────────────────────────────────────────

    // 依 sort_order 降序，最新在最上
    public List<AnalysisNoteDto> getByClientId(Integer clientId) {
        ensureClientExists(clientId);
        return noteRepo.findByClientIdOrderBySortOrderDesc(clientId).stream()
                .map(AnalysisNoteDto::from)
                .toList();
    }

    // ── POST ─────────────────────────────────────────────

    // sort_order 後端自動遞增，前端不需傳入
    public AnalysisNoteDto create(Integer clientId, AnalysisNoteDto dto) {
        ensureClientExists(clientId);
        validateTopic(dto.getTopic());

        Integer maxOrder = noteRepo.findMaxSortOrderByClientId(clientId);

        AnalysisNote n = new AnalysisNote();
        n.setClientId(clientId);
        n.setTitle(dto.getTitle());
        n.setContent(dto.getContent());
        n.setPlanetKey(dto.getPlanetKey());
        n.setSignKey(dto.getSignKey());
        n.setHouseKey(dto.getHouseKey());
        n.setTopic(dto.getTopic());
        n.setSortOrder(maxOrder + 1);

        return AnalysisNoteDto.from(noteRepo.save(n));
    }

    // ── PUT ──────────────────────────────────────────────

    // 更新 title / content / planetKey / signKey / houseKey / topic
    // sort_order 與 createdAt 不異動
    public AnalysisNoteDto update(Integer clientId, Integer nid, AnalysisNoteDto dto) {
        ensureClientExists(clientId);
        validateTopic(dto.getTopic());

        AnalysisNote n = noteRepo.findByClientIdAndId(clientId, nid)
                .orElseThrow(() -> new ResourceNotFoundException("AnalysisNote", nid));

        n.setTitle(dto.getTitle());
        n.setContent(dto.getContent());
        n.setPlanetKey(dto.getPlanetKey());
        n.setSignKey(dto.getSignKey());
        n.setHouseKey(dto.getHouseKey());
        n.setTopic(dto.getTopic());

        return AnalysisNoteDto.from(noteRepo.save(n));
    }

    // ── DELETE ───────────────────────────────────────────

    public void delete(Integer clientId, Integer nid) {
        ensureClientExists(clientId);
        AnalysisNote n = noteRepo.findByClientIdAndId(clientId, nid)
                .orElseThrow(() -> new ResourceNotFoundException("AnalysisNote", nid));
        noteRepo.delete(n);
    }

    // ── 驗證 ─────────────────────────────────────────────

    private void ensureClientExists(Integer clientId) {
        if (!clientRepo.existsById(clientId)) {
            throw new ResourceNotFoundException("Client", clientId);
        }
    }

    private void validateTopic(String topic) {
        if (topic != null && !VALID_TOPICS.contains(topic))
            throw new IllegalArgumentException("無效的 topic 值：" + topic
                    + "，允許值：general / career / love / wealth / challenge");
    }
}
