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

    // v8：改用降序（最新的在最上面）
    List<AnalysisNote> findByClientIdOrderBySortOrderDesc(Integer clientId);

    Optional<AnalysisNote> findByClientIdAndId(Integer clientId, Integer id);

    @Query("SELECT COALESCE(MAX(n.sortOrder), 0) FROM AnalysisNote n WHERE n.clientId = :clientId")
    Integer findMaxSortOrderByClientId(@Param("clientId") Integer clientId);
}
