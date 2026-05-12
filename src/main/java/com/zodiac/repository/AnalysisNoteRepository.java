package com.zodiac.repository;

import com.zodiac.model.AnalysisNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnalysisNoteRepository extends JpaRepository<AnalysisNote, Integer> {

    List<AnalysisNote> findByClientIdOrderBySortOrderAsc(Integer clientId);

    Optional<AnalysisNote> findByClientIdAndId(Integer clientId, Integer id);

    /** 取得該客戶目前最大的 sort_order，用於自動遞增 */
    @Query("SELECT COALESCE(MAX(n.sortOrder), 0) FROM AnalysisNote n WHERE n.clientId = :clientId")
    Integer findMaxSortOrderByClientId(@Param("clientId") Integer clientId);
}
