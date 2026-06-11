package com.project.zodiac_api.service;

import com.project.zodiac_api.dto.ElementNoteDto;
import com.project.zodiac_api.exception.ResourceNotFoundException;
import com.project.zodiac_api.model.ElementNote;
import com.project.zodiac_api.repository.CodeReferenceRepository;
import com.project.zodiac_api.repository.ElementNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ElementNoteService {

    private final ElementNoteRepository noteRepo;
    private final CodeReferenceRepository codeRepo; // 驗證用，從 DB 取代碼表

    // ── GET ─────────────────────────────────────────────────────────────────

    /**
     * 取得指定頁籤的解析列表
     *
     * @param signKey   星座代碼（必填）
     * @param planetKey 行星代碼（null = 純星座解析）
     * @param houseKey  宮位（null = 星座特性頁籤）
     */
    public List<ElementNoteDto> getByKeys(String signKey, String planetKey, Short houseKey) {
        validateKeys(signKey, planetKey, houseKey);
        return noteRepo.findByKeys(signKey, planetKey, houseKey).stream()
                .map(ElementNoteDto::from)
                .toList();
    }

    // ── POST ─────────────────────────────────────────────────────────────────

    /**
     * 新增解析段落
     * sort_order 後端自動遞增（取當前最大值 + 1），前端不需傳入
     */
    public ElementNoteDto create(String signKey, String planetKey, Short houseKey, ElementNoteDto dto) {
        validateKeys(signKey, planetKey, houseKey);

        Integer maxOrder = noteRepo.findMaxSortOrder(signKey, planetKey, houseKey);

        ElementNote n = new ElementNote();
        n.setSignKey(signKey);
        n.setPlanetKey(planetKey);
        n.setHouseKey(houseKey);
        n.setTitle(dto.getTitle());
        n.setContent(dto.getContent());
        n.setTag(dto.getTag());
        n.setSortOrder(maxOrder + 1);

        return ElementNoteDto.from(noteRepo.save(n));
    }

    // ── PUT ──────────────────────────────────────────────────────────────────

    /**
     * 更新解析段落
     * 僅更新 title / content / tag；sign_key / planet_key / house_key / sort_order 不異動
     */
    public ElementNoteDto update(Integer id, ElementNoteDto dto) {
        ElementNote n = noteRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ElementNote", id));

        n.setTitle(dto.getTitle());
        n.setContent(dto.getContent());
        n.setTag(dto.getTag());

        return ElementNoteDto.from(noteRepo.save(n));
    }

    // ── DELETE ───────────────────────────────────────────────────────────────

    public void delete(Integer id) {
        ElementNote n = noteRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ElementNote", id));
        noteRepo.delete(n);
    }

    // ── 驗證 ─────────────────────────────────────────────────────────────────

    /**
     * 從 code_references 表驗證代碼合法性
     * 不在 Service 硬寫白名單，代碼新增只改 DB 即可
     */
    private void validateKeys(String signKey, String planetKey, Short houseKey) {
        if (signKey == null || signKey.isBlank()) {
            throw new IllegalArgumentException("星座代碼不可為空");
        }
        if (!codeRepo.existsByCodeAndCategory(signKey, "sign")) {
            throw new IllegalArgumentException("無效的星座代碼：" + signKey);
        }
        if (planetKey != null && !codeRepo.existsByCodeAndCategory(planetKey, "planet")) {
            throw new IllegalArgumentException("無效的行星代碼：" + planetKey);
        }
        if (houseKey != null && (houseKey < 1 || houseKey > 12)) {
            throw new IllegalArgumentException("宮位必須介於 1~12");
        }
    }
}
