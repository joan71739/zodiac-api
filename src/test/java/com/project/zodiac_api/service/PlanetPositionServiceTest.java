package com.project.zodiac_api.service;

import com.project.zodiac_api.dto.PlanetPositionDto;
import com.project.zodiac_api.exception.ResourceNotFoundException;
import com.project.zodiac_api.model.PlanetPosition;
import com.project.zodiac_api.repository.ClientRepository;
import com.project.zodiac_api.repository.PlanetPositionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlanetPositionService 單元測試")
class PlanetPositionServiceTest {

    @Mock PlanetPositionRepository planetRepo;
    @Mock ClientRepository clientRepo;

    @InjectMocks PlanetPositionService planetService;

    @BeforeEach
    void setUp() {
        when(clientRepo.existsById(1)).thenReturn(true);
        when(clientRepo.existsById(999)).thenReturn(false);
    }

    // ────────────────────────────────────────────────────
    // GET
    // ────────────────────────────────────────────────────

    @Test
    @DisplayName("getByClientId — 回傳對應 client 的行星列表")
    void getByClientId_returnsListOfDtos() {
        PlanetPosition p = makePlanet(1, 1, "Q", false);  // 太陽
        when(planetRepo.findByClientId(1)).thenReturn(List.of(p));

        List<PlanetPositionDto> result = planetService.getByClientId(1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPlanet()).isEqualTo("Q");
        assertThat(result.get(0).getIsLord()).isFalse();
    }

    @Test
    @DisplayName("getByClientId — client 不存在時拋出 ResourceNotFoundException")
    void getByClientId_clientNotFound_throws() {
        assertThatThrownBy(() -> planetService.getByClientId(999))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ────────────────────────────────────────────────────
    // POST（createBatch）
    // ────────────────────────────────────────────────────

    @Test
    @DisplayName("createBatch — 先 deleteByClientId 再 saveAll（整批 11 筆）")
    void createBatch_deletesOldAndSavesAll() {
        List<PlanetPositionDto> dtos = makeDtoList(11);
        List<PlanetPosition> saved = makeEntityList(11, 1);
        when(planetRepo.saveAll(anyList())).thenReturn(saved);

        List<PlanetPositionDto> result = planetService.createBatch(1, dtos);

        verify(planetRepo, times(1)).deleteByClientId(1);
        verify(planetRepo, times(1)).saveAll(anyList());
        assertThat(result).hasSize(11);
    }

    @Test
    @DisplayName("createBatch — 含 isLord=true 的行星正確存入")
    void createBatch_withIsLordTrue_persistsFlag() {
        PlanetPositionDto lordDto = makeDto("R", true);      // 金星
        PlanetPosition lordEntity = makePlanet(10, 1, "R", true);
        when(planetRepo.saveAll(anyList())).thenReturn(List.of(lordEntity));

        List<PlanetPositionDto> result = planetService.createBatch(1, List.of(lordDto));

        assertThat(result.get(0).getIsLord()).isTrue();
        assertThat(result.get(0).getPlanet()).isEqualTo("R");
    }

    @Test
    @DisplayName("createBatch — client 不存在時拋出 ResourceNotFoundException")
    void createBatch_clientNotFound_throws() {
        assertThatThrownBy(() -> planetService.createBatch(999, List.of()))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(planetRepo, never()).saveAll(anyList());
    }

    // ────────────────────────────────────────────────────
    // PUT（update）— 命主星唯一性核心測試
    // ────────────────────────────────────────────────────

    @Test
    @DisplayName("update isLord=true — 先 clearLordByClientId 再 save（確保唯一性）")
    void update_withIsLordTrue_clearsThenSaves() {
        PlanetPosition existing = makePlanet(5, 1, "R", false);  // 金星
        when(planetRepo.findById(5)).thenReturn(Optional.of(existing));
        when(planetRepo.save(any())).thenReturn(existing);

        PlanetPositionDto dto = makeDto("R", true);
        planetService.update(1, 5, dto);

        var inOrder = inOrder(planetRepo);
        inOrder.verify(planetRepo).clearLordByClientId(1);
        inOrder.verify(planetRepo).save(existing);

        assertThat(existing.getIsLord()).isTrue();
    }

    @Test
    @DisplayName("update isLord=false — 不呼叫 clearLordByClientId，直接 save")
    void update_withIsLordFalse_doesNotClear() {
        PlanetPosition existing = makePlanet(5, 1, "R", true);
        when(planetRepo.findById(5)).thenReturn(Optional.of(existing));
        when(planetRepo.save(any())).thenReturn(existing);

        PlanetPositionDto dto = makeDto("R", false);
        planetService.update(1, 5, dto);

        verify(planetRepo, never()).clearLordByClientId(anyInt());
        verify(planetRepo, times(1)).save(existing);
        assertThat(existing.getIsLord()).isFalse();
    }

    @Test
    @DisplayName("update isLord=null — 視為 false，不呼叫 clearLordByClientId")
    void update_withIsLordNull_treatedAsFalse() {
        PlanetPosition existing = makePlanet(5, 1, "E", false);  // 水星
        when(planetRepo.findById(5)).thenReturn(Optional.of(existing));
        when(planetRepo.save(any())).thenReturn(existing);

        PlanetPositionDto dto = makeDto("E", null);
        planetService.update(1, 5, dto);

        verify(planetRepo, never()).clearLordByClientId(anyInt());
        assertThat(existing.getIsLord()).isFalse();
    }

    @Test
    @DisplayName("update — 行星 id 不存在時拋出 ResourceNotFoundException")
    void update_planetNotFound_throws() {
        when(planetRepo.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> planetService.update(1, 999, makeDto("Q", false)))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(planetRepo, never()).clearLordByClientId(anyInt());
        verify(planetRepo, never()).save(any());
    }

    @Test
    @DisplayName("update — 行星存在但 clientId 不符時拋出 ResourceNotFoundException")
    void update_planetBelongsToOtherClient_throws() {
        PlanetPosition wrongClient = makePlanet(5, 2, "Q", false);
        when(planetRepo.findById(5)).thenReturn(Optional.of(wrongClient));

        assertThatThrownBy(() -> planetService.update(1, 5, makeDto("Q", true)))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(planetRepo, never()).clearLordByClientId(anyInt());
    }

    @Test
    @DisplayName("update — client 不存在時拋出 ResourceNotFoundException")
    void update_clientNotFound_throws() {
        assertThatThrownBy(() -> planetService.update(999, 1, makeDto("Q", false)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ────────────────────────────────────────────────────
    // helper
    // ────────────────────────────────────────────────────

    private PlanetPosition makePlanet(int id, int clientId, String planet, boolean isLord) {
        PlanetPosition p = new PlanetPosition();
        p.setId(id);
        p.setClientId(clientId);
        p.setPlanet(planet);
        p.setSign("g");             // 獅子座代碼
        p.setDegreeNum((short) 10);
        p.setMinuteNum((short) 0);
        p.setHouse(1);
        p.setIsLord(isLord);
        return p;
    }

    private PlanetPositionDto makeDto(String planet, Boolean isLord) {
        PlanetPositionDto dto = new PlanetPositionDto();
        dto.setPlanet(planet);
        dto.setSign("g");           // 獅子座代碼
        dto.setDegreeNum((short) 10);
        dto.setMinuteNum((short) 0);
        dto.setHouse(1);
        dto.setIsLord(isLord);
        return dto;
    }

    private List<PlanetPositionDto> makeDtoList(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> makeDto("Q", false))
                .toList();
    }

    private List<PlanetPosition> makeEntityList(int count, int clientId) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> makePlanet(i + 1, clientId, "Q", false))
                .toList();
    }
}
