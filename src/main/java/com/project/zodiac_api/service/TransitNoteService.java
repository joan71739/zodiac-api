package com.project.zodiac_api.service;

import com.project.zodiac_api.dto.TransitNoteDto;
import com.project.zodiac_api.exception.ResourceNotFoundException;
import com.project.zodiac_api.model.TransitNote;
import com.project.zodiac_api.repository.CodeReferenceRepository;
import com.project.zodiac_api.repository.TransitNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TransitNoteService {

    private final TransitNoteRepository noteRepo;
    private final CodeReferenceRepository codeRepo;

    private static final Set<String> VALID_ASPECTS = Set.of("q", "w", "e", "r", "t");
    private static final Set<String> VALID_TOPICS  = Set.of(
            "general", "career", "love", "wealth", "challenge"
    );

    // ── GET ──────────────────────────────────────────────────

    public List<TransitNoteDto> getByKeys(
            String transitPlanet,
            String aspectType,
            String natalPlanet,
            Short  transitHouse) {

        validateKeys(transitPlanet, aspectType, natalPlanet, transitHouse);
        return noteRepo.findByKeys(transitPlanet, aspectType, natalPlanet, transitHouse)
                .stream()
                .map(TransitNoteDto::from)
                .toList();
    }

    // ── POST ─────────────────────────────────────────────────

    public TransitNoteDto create(
            String transitPlanet,
            String aspectType,
            String natalPlanet,
            Short  transitHouse,
            TransitNoteDto dto) {

        validateKeys(transitPlanet, aspectType, natalPlanet, transitHouse);
        validateTopic(dto.getTopic());

        Integer maxOrder = noteRepo.findMaxSortOrder(
                transitPlanet, aspectType, natalPlanet, transitHouse);

        TransitNote n = new TransitNote();
        n.setTransitPlanet(transitPlanet);
        n.setAspectType(aspectType);
        n.setNatalPlanet(natalPlanet);
        n.setTransitHouse(transitHouse);
        n.setTitle(dto.getTitle());
        n.setContent(dto.getContent());
        n.setTag(dto.getTag());
        n.setTopic(dto.getTopic());
        n.setSortOrder(maxOrder + 1);

        return TransitNoteDto.from(noteRepo.save(n));
    }

    // ── PUT ──────────────────────────────────────────────────

    public TransitNoteDto update(Integer id, TransitNoteDto dto) {
        validateTopic(dto.getTopic());

        TransitNote n = noteRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TransitNote", id));

        n.setTitle(dto.getTitle());
        n.setContent(dto.getContent());
        n.setTag(dto.getTag());
        n.setTopic(dto.getTopic());

        return TransitNoteDto.from(noteRepo.save(n));
    }

    // ── DELETE ───────────────────────────────────────────────

    public void delete(Integer id) {
        TransitNote n = noteRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TransitNote", id));
        noteRepo.delete(n);
    }

    // ── 驗證 ─────────────────────────────────────────────────

    private void validateKeys(
            String transitPlanet,
            String aspectType,
            String natalPlanet,
            Short  transitHouse) {

        // 1. 行運行星必填
        if (transitPlanet == null || transitPlanet.isBlank())
            throw new IllegalArgumentException("行運行星代碼不可為空");
        if (!codeRepo.existsByCodeAndCategory(transitPlanet, "planet"))
            throw new IllegalArgumentException("無效的行運行星代碼：" + transitPlanet);

        // 2. aspect_type 與 natal_planet 必須同時有值或同時為 NULL
        boolean hasAspect = aspectType   != null && !aspectType.isBlank();
        boolean hasNatal  = natalPlanet  != null && !natalPlanet.isBlank();
        if (hasAspect != hasNatal)
            throw new IllegalArgumentException("aspectType 與 natalPlanet 必須同時填寫或同時為空");

        // 3. aspect_type 若有值須合法
        if (hasAspect && !VALID_ASPECTS.contains(aspectType))
            throw new IllegalArgumentException("無效的相位代碼：" + aspectType
                    + "，允許值：q / w / e / r / t");

        // 4. natal_planet 若有值須合法
        if (hasNatal && !codeRepo.existsByCodeAndCategory(natalPlanet, "planet"))
            throw new IllegalArgumentException("無效的本命星代碼：" + natalPlanet);

        // 5. transit_house 若有值須在 1~12（DB 也有 CHECK，這裡提前給友善訊息）
        if (transitHouse != null && (transitHouse < 1 || transitHouse > 12))
            throw new IllegalArgumentException("宮位必須介於 1~12");
    }

    private void validateTopic(String topic) {
        if (topic != null && !VALID_TOPICS.contains(topic))
            throw new IllegalArgumentException("無效的 topic 值：" + topic
                    + "，允許值：general / career / love / wealth / challenge");
    }
}
