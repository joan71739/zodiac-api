package com.project.zodiac_api.service;

import com.project.zodiac_api.dto.ElementNoteDto;
import com.project.zodiac_api.exception.ResourceNotFoundException;
import com.project.zodiac_api.model.ElementNote;
import com.project.zodiac_api.repository.CodeReferenceRepository;
import com.project.zodiac_api.repository.ElementNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ElementNoteService {

    private final ElementNoteRepository noteRepo;
    private final CodeReferenceRepository codeRepo;

    // topic 值域（固定小，不需查 DB）
    private static final Set<String> VALID_TOPICS = Set.of(
            "general", "career", "love", "wealth", "challenge"
    );

    // ── GET ──────────────────────────────────────────────────

    public List<ElementNoteDto> getByKeys(String signKey, String planetKey, Short houseKey) {
        validateKeys(signKey, planetKey, houseKey);
        return noteRepo.findByKeys(signKey, planetKey, houseKey).stream()
                .map(ElementNoteDto::from)
                .toList();
    }

    // ── POST ─────────────────────────────────────────────────

    public ElementNoteDto create(String signKey, String planetKey, Short houseKey, ElementNoteDto dto) {
        validateKeys(signKey, planetKey, houseKey);
        validateTopic(dto.getTopic());

        Integer maxOrder = noteRepo.findMaxSortOrder(signKey, planetKey, houseKey);

        ElementNote n = new ElementNote();
        n.setSignKey(signKey);
        n.setPlanetKey(planetKey);
        n.setHouseKey(houseKey);
        n.setTitle(dto.getTitle());
        n.setContent(dto.getContent());
        n.setTag(dto.getTag());
        n.setTopic(dto.getTopic());
        n.setSortOrder(maxOrder + 1);

        return ElementNoteDto.from(noteRepo.save(n));
    }

    // ── PUT ──────────────────────────────────────────────────

    public ElementNoteDto update(Integer id, ElementNoteDto dto) {
        validateTopic(dto.getTopic());

        ElementNote n = noteRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ElementNote", id));

        n.setTitle(dto.getTitle());
        n.setContent(dto.getContent());
        n.setTag(dto.getTag());
        n.setTopic(dto.getTopic());

        return ElementNoteDto.from(noteRepo.save(n));
    }

    // ── DELETE ───────────────────────────────────────────────

    public void delete(Integer id) {
        ElementNote n = noteRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ElementNote", id));
        noteRepo.delete(n);
    }

    // ── 驗證 ─────────────────────────────────────────────────

    private void validateKeys(String signKey, String planetKey, Short houseKey) {
        if (signKey == null || signKey.isBlank())
            throw new IllegalArgumentException("星座代碼不可為空");
        if (!codeRepo.existsByCodeAndCategory(signKey, "sign"))
            throw new IllegalArgumentException("無效的星座代碼：" + signKey);
        if (planetKey != null && !codeRepo.existsByCodeAndCategory(planetKey, "planet"))
            throw new IllegalArgumentException("無效的行星代碼：" + planetKey);
        if (houseKey != null && (houseKey < 1 || houseKey > 12))
            throw new IllegalArgumentException("宮位必須介於 1~12");
    }

    private void validateTopic(String topic) {
        // topic 允許 null（未分類），有值才驗證
        if (topic != null && !VALID_TOPICS.contains(topic))
            throw new IllegalArgumentException("無效的 topic 值：" + topic
                    + "，允許值：general / career / love / wealth / challenge");
    }
}
