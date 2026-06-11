package com.project.zodiac_api.service;

import com.project.zodiac_api.dto.ClientListDto;
import com.project.zodiac_api.dto.ClientRequestDto;
import com.project.zodiac_api.dto.ClientResponseDto;
import com.project.zodiac_api.exception.ResourceNotFoundException;
import com.project.zodiac_api.model.Client;
import com.project.zodiac_api.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClientService 單元測試")
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepo;

    @InjectMocks
    private ClientService clientService;

    private Client sampleClient;

    @BeforeEach
    void setUp() {
        sampleClient = new Client();
        sampleClient.setId(1);
        sampleClient.setName("王小明");
        sampleClient.setBirthDate(LocalDate.of(1993, 8, 10));
        sampleClient.setBirthTime(LocalTime.of(8, 30));
        sampleClient.setBirthPlace("台北");
        sampleClient.setAscSign("j");       // 天秤座
        sampleClient.setAscDegreeNum(22);
        sampleClient.setAscMinuteNum(1);
        sampleClient.setMcSign("f");        // 巨蟹座
        sampleClient.setMcDegreeNum(22);
        sampleClient.setMcMinuteNum(35);
    }

    // ── GET ALL ─────────────────────────────────────────

    @Test
    @DisplayName("取得所有客戶 — 回傳正確數量（不含 ASC/MC）")
    void getAll_returnsAllClients() {
        when(clientRepo.findAll()).thenReturn(List.of(sampleClient));

        List<ClientListDto> result = clientService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("王小明");
        verify(clientRepo, times(1)).findAll();
    }

    @Test
    @DisplayName("無客戶資料時回傳空列表")
    void getAll_emptyList() {
        when(clientRepo.findAll()).thenReturn(List.of());

        List<ClientListDto> result = clientService.getAll();

        assertThat(result).isEmpty();
    }

    // ── GET BY ID ───────────────────────────────────────

    @Test
    @DisplayName("以 id 取得客戶 — 存在時正確回傳（含 ASC/MC）")
    void getById_existing_returnsClient() {
        when(clientRepo.findById(1)).thenReturn(Optional.of(sampleClient));

        ClientResponseDto result = clientService.getById(1);

        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("王小明");
        assertThat(result.getBirthPlace()).isEqualTo("台北");
        assertThat(result.getAscSign()).isEqualTo("j");   // 天秤座代碼
        assertThat(result.getAscDegreeNum()).isEqualTo(22);
        assertThat(result.getMcSign()).isEqualTo("f");    // 巨蟹座代碼
    }

    @Test
    @DisplayName("以 id 取得客戶 — 不存在時拋出 ResourceNotFoundException")
    void getById_notFound_throwsException() {
        when(clientRepo.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.getById(999))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── CREATE ──────────────────────────────────────────

    @Test
    @DisplayName("新增客戶 — 正確儲存並回傳（含四軸資訊）")
    void create_savesAndReturnsClient() {
        ClientRequestDto req = new ClientRequestDto();
        req.setName("林小美");
        req.setBirthDate(LocalDate.of(1995, 3, 15));
        req.setBirthTime(LocalTime.of(12, 0));
        req.setBirthPlace("高雄");
        req.setAscSign("l");        // 射手座
        req.setAscDegreeNum(5);
        req.setAscMinuteNum(10);
        req.setMcSign("h");         // 處女座
        req.setMcDegreeNum(3);
        req.setMcMinuteNum(45);

        Client saved = new Client();
        saved.setId(2);
        saved.setName("林小美");
        saved.setBirthDate(req.getBirthDate());
        saved.setBirthTime(req.getBirthTime());
        saved.setBirthPlace("高雄");
        saved.setAscSign("l");
        saved.setAscDegreeNum(5);
        saved.setAscMinuteNum(10);
        saved.setMcSign("h");
        saved.setMcDegreeNum(3);
        saved.setMcMinuteNum(45);

        when(clientRepo.save(any(Client.class))).thenReturn(saved);

        ClientResponseDto result = clientService.create(req);

        assertThat(result.getId()).isEqualTo(2);
        assertThat(result.getName()).isEqualTo("林小美");
        assertThat(result.getAscSign()).isEqualTo("l");   // 射手座代碼
        assertThat(result.getMcSign()).isEqualTo("h");    // 處女座代碼
        verify(clientRepo, times(1)).save(any(Client.class));
    }

    @Test
    @DisplayName("新增客戶 — 四軸資訊為 null 時仍可正常儲存")
    void create_withNullAscMc_savesSuccessfully() {
        ClientRequestDto req = new ClientRequestDto();
        req.setName("陳小華");
        req.setBirthDate(LocalDate.of(2000, 1, 1));
        req.setBirthPlace("台中");

        Client saved = new Client();
        saved.setId(3);
        saved.setName("陳小華");
        saved.setBirthDate(req.getBirthDate());
        saved.setBirthPlace("台中");

        when(clientRepo.save(any(Client.class))).thenReturn(saved);

        ClientResponseDto result = clientService.create(req);

        assertThat(result.getId()).isEqualTo(3);
        assertThat(result.getAscSign()).isNull();
        assertThat(result.getMcSign()).isNull();
    }

    // ── UPDATE ──────────────────────────────────────────

    @Test
    @DisplayName("編輯客戶 — 正確更新欄位（含四軸資訊）")
    void update_updatesFieldsCorrectly() {
        when(clientRepo.findById(1)).thenReturn(Optional.of(sampleClient));
        when(clientRepo.save(any(Client.class))).thenReturn(sampleClient);

        ClientRequestDto req = new ClientRequestDto();
        req.setName("王大明");
        req.setBirthDate(LocalDate.of(1993, 8, 10));
        req.setBirthTime(LocalTime.of(8, 30));
        req.setBirthPlace("新北");
        req.setAscSign("x");        // 水瓶座
        req.setAscDegreeNum(10);
        req.setAscMinuteNum(5);
        req.setMcSign("k");         // 天蠍座
        req.setMcDegreeNum(8);
        req.setMcMinuteNum(20);

        clientService.update(1, req);

        assertThat(sampleClient.getName()).isEqualTo("王大明");
        assertThat(sampleClient.getBirthPlace()).isEqualTo("新北");
        assertThat(sampleClient.getAscSign()).isEqualTo("x");   // 水瓶座代碼
        assertThat(sampleClient.getAscDegreeNum()).isEqualTo(10);
        assertThat(sampleClient.getMcSign()).isEqualTo("k");    // 天蠍座代碼
        assertThat(sampleClient.getMcDegreeNum()).isEqualTo(8);
    }

    @Test
    @DisplayName("編輯客戶 — 不存在時拋出 ResourceNotFoundException")
    void update_notFound_throwsException() {
        when(clientRepo.findById(999)).thenReturn(Optional.empty());

        ClientRequestDto req = new ClientRequestDto();
        req.setName("不存在");

        assertThatThrownBy(() -> clientService.update(999, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── DELETE ──────────────────────────────────────────

    @Test
    @DisplayName("刪除客戶 — 正確呼叫 repository")
    void delete_existingClient_callsRepo() {
        when(clientRepo.findById(1)).thenReturn(Optional.of(sampleClient));

        clientService.delete(1);

        verify(clientRepo, times(1)).deleteById(1);
    }

    @Test
    @DisplayName("刪除客戶 — 不存在時拋出 ResourceNotFoundException")
    void delete_notFound_throwsException() {
        when(clientRepo.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.delete(999))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
