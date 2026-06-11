package com.project.zodiac_api.service;

import com.project.zodiac_api.dto.ReferencePanelDto;
import com.project.zodiac_api.dto.ReferencePanelDto.*;
import com.project.zodiac_api.exception.ResourceNotFoundException;
import com.project.zodiac_api.model.ElementNote;
import com.project.zodiac_api.model.HouseRuler;
import com.project.zodiac_api.model.PlanetPosition;
import com.project.zodiac_api.repository.ClientRepository;
import com.project.zodiac_api.repository.CodeReferenceRepository;
import com.project.zodiac_api.repository.ElementNoteRepository;
import com.project.zodiac_api.repository.HouseRulerRepository;
import com.project.zodiac_api.repository.PlanetPositionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReferencePanelService {

    private final ClientRepository         clientRepo;
    private final HouseRulerRepository     houseRepo;
    private final PlanetPositionRepository planetRepo;
    private final ElementNoteRepository    elementNoteRepo;
    private final CodeReferenceRepository  codeRepo;

    // 只抓個人行星（速度快，有實質意義的本命星）
    private static final Set<String> PERSONAL_PLANETS = Set.of("Q", "W", "E", "R", "T");

    // 啟動時從 DB 載入代碼對照表，避免每次 API 呼叫都查 DB
    private Map<String, String> planetLabels;
    private Map<String, String> signLabels;

    @PostConstruct
    public void init() {
        planetLabels = codeRepo.findByCategory("planet").stream()
                .collect(Collectors.toMap(c -> c.getCode(), c -> c.getZhName()));
        signLabels = codeRepo.findByCategory("sign").stream()
                .collect(Collectors.toMap(c -> c.getCode(), c -> c.getZhName()));
    }

    /**
     * 組裝指定客戶的解析參考面板
     */
    public ReferencePanelDto buildPanel(Integer clientId) {
        if (!clientRepo.existsById(clientId)) {
            throw new ResourceNotFoundException("Client", clientId);
        }

        ReferencePanelDto panel = new ReferencePanelDto();
        panel.setAscSection(buildAscSection(clientId));
        panel.setPlanetSections(buildPlanetSections(clientId));
        return panel;
    }

    // ── 上升區塊 ──────────────────────────────────────────────────────────

    private AscSectionDto buildAscSection(Integer clientId) {
        List<HouseRuler> houses = houseRepo.findByClientIdOrderByHouseNumberAsc(clientId);

        // 取第一宮的 houseSign 作為上升星座（整宮制下一宮 = 上升）
        String ascSign = houses.stream()
                .filter(h -> h.getHouseNumber() == 1 && h.getHouseSign() != null)
                .map(HouseRuler::getHouseSign)
                .findFirst()
                .orElse(null);

        List<HouseItemDto> houseItems = houses.stream()
                .filter(h -> h.getHouseSign() != null && !h.getHouseSign().isBlank())
                .map(h -> {
                    String sign = h.getHouseSign();
                    List<NoteItemDto> notes = toNoteItems(
                        elementNoteRepo.findByKeys(sign, null, null)
                    );
                    return new HouseItemDto(
                        h.getHouseNumber(),
                        sign,
                        signLabels.getOrDefault(sign, sign),
                        notes
                    );
                })
                .toList();

        return new AscSectionDto(
            ascSign,
            ascSign != null ? signLabels.getOrDefault(ascSign, ascSign) : null,
            houseItems
        );
    }

    // ── 行星區塊 ──────────────────────────────────────────────────────────

    private List<PlanetSectionDto> buildPlanetSections(Integer clientId) {
        return planetRepo.findByClientIdOrderByIdAsc(clientId).stream()
                .filter(p -> p.getPlanet() != null
                          && PERSONAL_PLANETS.contains(p.getPlanet())
                          && p.getSign()   != null
                          && p.getHouse()  != null)
                .map(this::buildPlanetSection)
                .toList();
    }

    private PlanetSectionDto buildPlanetSection(PlanetPosition p) {
        String planet   = p.getPlanet();
        String sign     = p.getSign();
        Short  houseKey = p.getHouse().shortValue();

        // 第一層：星座特性（planet_key=NULL, house_key=NULL）
        List<NoteItemDto> signNotes =
            toNoteItems(elementNoteRepo.findByKeys(sign, null, null));

        // 第二層：星座×宮位（planet_key=NULL, house_key=行星所在宮）
        List<NoteItemDto> signHouseNotes =
            toNoteItems(elementNoteRepo.findByKeys(sign, null, houseKey));

        // 第三層：行星×星座×宮位（planet_key=行星, house_key=行星所在宮）
        List<NoteItemDto> planetSignHouseNotes =
            toNoteItems(elementNoteRepo.findByKeys(sign, planet, houseKey));

        return new PlanetSectionDto(
            planet,
            planetLabels.getOrDefault(planet, planet),
            sign,
            signLabels.getOrDefault(sign, sign),
            p.getHouse(),
            signNotes,
            signHouseNotes,
            planetSignHouseNotes
        );
    }

    // ── helper ────────────────────────────────────────────────────────────

    private List<NoteItemDto> toNoteItems(List<ElementNote> notes) {
        return notes.stream()
                .map(n -> new NoteItemDto(n.getId(), n.getTitle(), n.getContent(), n.getTopic()))
                .toList();
    }
}
