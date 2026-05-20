package com.project.zodiac_api.repository;

import com.project.zodiac_api.model.AnalysisNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnalysisNoteRepository extends JpaRepository<AnalysisNote, Integer> {

    // 依 sort_order 降序，最新在最上
    List<AnalysisNote> findByClientIdOrderBySortOrderDesc(Integer clientId);

    Optional<AnalysisNote> findByClientIdAndId(Integer clientId, Integer id);

    // 新增時取得當前最大 sort_order；無資料時 COALESCE 回傳 0，確保首筆 sort_order = 1
    @Query("SELECT COALESCE(MAX(n.sortOrder), 0) FROM AnalysisNote n WHERE n.clientId = :clientId")
    Integer findMaxSortOrderByClientId(@Param("clientId") Integer clientId);
}
