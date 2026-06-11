package com.project.zodiac_api.service;

import com.project.zodiac_api.dto.HouseRulerDto;
import com.project.zodiac_api.exception.ResourceNotFoundException;
import com.project.zodiac_api.model.HouseRuler;
import com.project.zodiac_api.repository.ClientRepository;
import com.project.zodiac_api.repository.HouseRulerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HouseRulerService 單元測試")
class HouseRulerServiceTest {

    @Mock HouseRulerRepository houseRepo;
    @Mock ClientRepository clientRepo;

    @InjectMocks HouseRulerService houseService;

    @BeforeEach
    void setUp() {
        when(clientRepo.existsById(1)).thenReturn(true);
        when(clientRepo.existsById(999)).thenReturn(false);
    }

    // ────────────────────────────────────────────────────
    // GET
    // ────────────────────────────────────────────────────

    @Test
    @DisplayName("getByClientId — 依 houseNumber 升序回傳 12 筆")
    void getByClientId_returns12OrderedHouses() {
        List<HouseRuler> entities = makeEntityList(12, 1);
        when(houseRepo.findByClientIdOrderByHouseNumberAsc(1)).thenReturn(entities);

        List<HouseRulerDto> result = houseService.getByClientId(1);

        assertThat(result).hasSize(12);
        assertThat(result.get(0).getHouseNumber()).isEqualTo(1);
        assertThat(result.get(11).getHouseNumber()).isEqualTo(12);
    }

    @Test
    @DisplayName("getByClientId — client 不存在時拋出 ResourceNotFoundException")
    void getByClientId_clientNotFound_throws() {
        assertThatThrownBy(() -> houseService.getByClientId(999))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(houseRepo, never()).findByClientIdOrderByHouseNumberAsc(any());
    }

    // ────────────────────────────────────────────────────
    // POST（createBatch）
    // ────────────────────────────────────────────────────

    @Test
    @DisplayName("createBatch — 先 deleteByClientId 再 saveAll（整批 12 筆）")
    void createBatch_deletesOldAndSavesAll() {
        List<HouseRulerDto> dtos = makeDtoList(12);
        List<HouseRuler> saved = makeEntityList(12, 1);
        when(houseRepo.saveAll(anyList())).thenReturn(saved);

        List<HouseRulerDto> result = houseService.createBatch(1, dtos);

        verify(houseRepo, times(1)).deleteByClientId(1);
        verify(houseRepo, times(1)).saveAll(anyList());
        assertThat(result).hasSize(12);
    }

    @Test
    @DisplayName("createBatch — saveAll 回傳亂序時，後端回傳照 saveAll 原始順序（前端負責 merge 排序）")
    void createBatch_savesInInputOrder() {
        List<HouseRuler> shuffled = new ArrayList<>(makeEntityList(12, 1));
        Collections.reverse(shuffled);
        when(houseRepo.saveAll(anyList())).thenReturn(shuffled);

        List<HouseRulerDto> result = houseService.createBatch(1, makeDtoList(12));

        assertThat(result).hasSize(12);
        assertThat(result.get(0).getHouseNumber()).isEqualTo(12);
    }

    @Test
    @DisplayName("createBatch — client 不存在時拋出 ResourceNotFoundException")
    void createBatch_clientNotFound_throws() {
        assertThatThrownBy(() -> houseService.createBatch(999, List.of()))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(houseRepo, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("createBatch — 傳入 12 筆 dto，saveAll 接收到 12 筆 entity")
    void createBatch_maps12DtosToEntities() {
        List<HouseRulerDto> dtos = makeDtoList(12);
        when(houseRepo.saveAll(anyList())).thenReturn(makeEntityList(12, 1));

        houseService.createBatch(1, dtos);

        verify(houseRepo).saveAll(argThat(list ->
            ((List<?>) list).size() == 12
        ));
    }

    // ────────────────────────────────────────────────────
    // PUT（update）
    // ────────────────────────────────────────────────────

    @Test
    @DisplayName("update — 正常更新單筆宮位守護星（含 houseSign）")
    void update_returnsUpdatedDto() {
        // v18：makeEntity 加入 houseSign 參數（天秤座 = 'j'）
        HouseRuler existing = makeEntity(1, 1, "j", "Q", "g", 5);
        when(houseRepo.findByClientIdAndId(1, 1)).thenReturn(Optional.of(existing));
        when(houseRepo.save(any())).thenReturn(existing);

        HouseRulerDto dto = new HouseRulerDto();
        dto.setHouseNumber(1);
        dto.setHouseSign("j");      // 天秤座
        dto.setRulingPlanet("W");   // 月亮
        dto.setFliesToSign("f");    // 巨蟹座
        dto.setFliesToHouse(4);

        houseService.update(1, 1, dto);

        assertThat(existing.getHouseSign()).isEqualTo("j");
        assertThat(existing.getRulingPlanet()).isEqualTo("W");
        assertThat(existing.getFliesToSign()).isEqualTo("f");
        assertThat(existing.getFliesToHouse()).isEqualTo(4);
        verify(houseRepo, times(1)).save(existing);
    }

    @Test
    @DisplayName("update — houseSign 為 null 時仍可正常儲存")
    void update_nullHouseSign_savesSuccessfully() {
        HouseRuler existing = makeEntity(1, 1, null, "Q", "g", 5);
        when(houseRepo.findByClientIdAndId(1, 1)).thenReturn(Optional.of(existing));
        when(houseRepo.save(any())).thenReturn(existing);

        HouseRulerDto dto = new HouseRulerDto();
        dto.setHouseNumber(1);
        dto.setHouseSign(null);
        dto.setRulingPlanet("Q");

        houseService.update(1, 1, dto);

        assertThat(existing.getHouseSign()).isNull();
        verify(houseRepo, times(1)).save(existing);
    }

    @Test
    @DisplayName("update — 宮位 id 不存在時拋出 ResourceNotFoundException")
    void update_houseNotFound_throws() {
        when(houseRepo.findByClientIdAndId(1, 999)).thenReturn(Optional.empty());

        HouseRulerDto dto = new HouseRulerDto();
        dto.setHouseNumber(1);

        assertThatThrownBy(() -> houseService.update(1, 999, dto))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(houseRepo, never()).save(any());
    }

    @Test
    @DisplayName("update — client 不存在時拋出 ResourceNotFoundException")
    void update_clientNotFound_throws() {
        HouseRulerDto dto = new HouseRulerDto();
        dto.setHouseNumber(1);

        assertThatThrownBy(() -> houseService.update(999, 1, dto))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(houseRepo, never()).findByClientIdAndId(any(), any());
    }

    // ────────────────────────────────────────────────────
    // helper
    // ────────────────────────────────────────────────────

    /**
     * v18：新增 houseSign 參數
     * @param houseSign 宮位起始星座代碼（可為 null）
     */
    private HouseRuler makeEntity(int id, int houseNumber, String houseSign,
                                  String planet, String sign, Integer fliesToHouse) {
        HouseRuler h = new HouseRuler();
        h.setId(id);
        h.setClientId(1);
        h.setHouseNumber(houseNumber);
        h.setHouseSign(houseSign);
        h.setRulingPlanet(planet);
        h.setFliesToSign(sign);
        h.setFliesToHouse(fliesToHouse);
        return h;
    }

    private List<HouseRuler> makeEntityList(int count, int clientId) {
        return java.util.stream.IntStream.rangeClosed(1, count)
                .mapToObj(i -> {
                    HouseRuler h = new HouseRuler();
                    h.setId(i);
                    h.setClientId(clientId);
                    h.setHouseNumber(i);
                    return h;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    private List<HouseRulerDto> makeDtoList(int count) {
        return java.util.stream.IntStream.rangeClosed(1, count)
                .mapToObj(i -> {
                    HouseRulerDto dto = new HouseRulerDto();
                    dto.setHouseNumber(i);
                    return dto;
                })
                .toList();
    }
}
