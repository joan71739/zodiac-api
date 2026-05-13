package com.project.zodiac_api.service;

import com.project.zodiac_api.dto.AnalysisNoteDto;
import com.project.zodiac_api.exception.ResourceNotFoundException;
import com.project.zodiac_api.model.AnalysisNote;
import com.project.zodiac_api.repository.AnalysisNoteRepository;
import com.project.zodiac_api.repository.ClientRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalysisNoteService 單元測試")
class AnalysisNoteServiceTest {

    @Mock
    private AnalysisNoteRepository noteRepo;

    @Mock
    private ClientRepository clientRepo;

    @InjectMocks
    private AnalysisNoteService noteService;

    // ── GET ─────────────────────────────────────────────

    @Test
    @DisplayName("取得解析列表 — 客戶不存在時拋出例外")
    void getByClientId_clientNotFound_throws() {
        when(clientRepo.existsById(99)).thenReturn(false);

        assertThatThrownBy(() -> noteService.getByClientId(99))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("取得解析列表 — 依 sortOrder 降序回傳（最新在最上面）")
    void getByClientId_returnsSortedListDesc() {
        when(clientRepo.existsById(1)).thenReturn(true);

        // sortOrder 高的（較新）排前面
        AnalysisNote n2 = makeNote(2, 1, "事業", "...", 2);
        AnalysisNote n1 = makeNote(1, 1, "感情", "...", 1);
        when(noteRepo.findByClientIdOrderBySortOrderDesc(1)).thenReturn(List.of(n2, n1));

        List<AnalysisNoteDto> result = noteService.getByClientId(1);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("事業");   // sortOrder=2，最新
        assertThat(result.get(1).getTitle()).isEqualTo("感情");   // sortOrder=1，較舊
    }

    // ── CREATE — sortOrder 自動遞增 ──────────────────────

    @Test
    @DisplayName("新增解析 — 首筆時 sortOrder = 1")
    void create_firstNote_sortOrderIsOne() {
        when(clientRepo.existsById(1)).thenReturn(true);
        when(noteRepo.findMaxSortOrderByClientId(1)).thenReturn(0);   // 無資料時 COALESCE 回 0

        AnalysisNote saved = makeNote(10, 1, "第一筆", "內容", 1);
        when(noteRepo.save(any(AnalysisNote.class))).thenReturn(saved);

        AnalysisNoteDto dto = new AnalysisNoteDto();
        dto.setTitle("第一筆");
        dto.setContent("內容");

        noteService.create(1, dto);

        ArgumentCaptor<AnalysisNote> captor = ArgumentCaptor.forClass(AnalysisNote.class);
        verify(noteRepo).save(captor.capture());
        assertThat(captor.getValue().getSortOrder()).isEqualTo(1);
    }

    @Test
    @DisplayName("新增解析 — 已有 3 筆時 sortOrder = 4")
    void create_existingNotes_sortOrderIncrementsCorrectly() {
        when(clientRepo.existsById(1)).thenReturn(true);
        when(noteRepo.findMaxSortOrderByClientId(1)).thenReturn(3);

        AnalysisNote saved = makeNote(11, 1, "第四筆", "內容", 4);
        when(noteRepo.save(any(AnalysisNote.class))).thenReturn(saved);

        AnalysisNoteDto dto = new AnalysisNoteDto();
        dto.setTitle("第四筆");
        dto.setContent("內容");

        noteService.create(1, dto);

        ArgumentCaptor<AnalysisNote> captor = ArgumentCaptor.forClass(AnalysisNote.class);
        verify(noteRepo).save(captor.capture());
        assertThat(captor.getValue().getSortOrder()).isEqualTo(4);
    }

    // ── UPDATE ──────────────────────────────────────────

    @Test
    @DisplayName("編輯解析 — 正確更新 title 與 content")
    void update_updatesCorrectly() {
        when(clientRepo.existsById(1)).thenReturn(true);
        AnalysisNote existing = makeNote(5, 1, "舊標題", "舊內容", 1);
        when(noteRepo.findByClientIdAndId(1, 5)).thenReturn(Optional.of(existing));
        when(noteRepo.save(any())).thenReturn(existing);

        AnalysisNoteDto req = new AnalysisNoteDto();
        req.setTitle("新標題");
        req.setContent("新內容");

        noteService.update(1, 5, req);

        assertThat(existing.getTitle()).isEqualTo("新標題");
        assertThat(existing.getContent()).isEqualTo("新內容");
    }

    @Test
    @DisplayName("編輯解析 — 不存在時拋出例外")
    void update_notFound_throws() {
        when(clientRepo.existsById(1)).thenReturn(true);
        when(noteRepo.findByClientIdAndId(1, 999)).thenReturn(Optional.empty());

        AnalysisNoteDto req = new AnalysisNoteDto();
        assertThatThrownBy(() -> noteService.update(1, 999, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── DELETE ──────────────────────────────────────────

    @Test
    @DisplayName("刪除解析 — 正確呼叫 delete")
    void delete_callsRepository() {
        when(clientRepo.existsById(1)).thenReturn(true);
        AnalysisNote existing = makeNote(5, 1, "標題", "內容", 1);
        when(noteRepo.findByClientIdAndId(1, 5)).thenReturn(Optional.of(existing));

        noteService.delete(1, 5);

        verify(noteRepo, times(1)).delete(existing);
    }

    // ── helper ──────────────────────────────────────────

    private AnalysisNote makeNote(int id, int clientId, String title, String content, int sortOrder) {
        AnalysisNote n = new AnalysisNote();
        n.setId(id);
        n.setClientId(clientId);
        n.setTitle(title);
        n.setContent(content);
        n.setSortOrder(sortOrder);
        return n;
    }
}
