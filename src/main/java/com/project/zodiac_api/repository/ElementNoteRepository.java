package com.project.zodiac_api.repository;

import com.project.zodiac_api.model.ElementNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ElementNoteRepository extends JpaRepository<ElementNote, Integer> {

    // ── 核心查詢 ────────────────────────────────────────────────────────────

    /**
     * 查詢指定頁籤的解析列表，依 sort_order 降序（最新在最上）
     *
     * 使用 IS NOT DISTINCT FROM 處理 NULL 比對：
     *   - planet_key=NULL  → 星座解析
     *   - planet_key='Q'   → 太陽×星座解析
     *   - house_key=NULL   → 星座特性頁籤
     *   - house_key=1      → 一宮頁籤
     *
     * 標準 SQL 的 NULL = NULL 為 false，IS NOT DISTINCT FROM 才能正確比對 NULL
     */
    @Query("""
        SELECT n FROM ElementNote n
        WHERE n.signKey = :signKey
          AND (n.planetKey IS NOT DISTINCT FROM :planetKey)
          AND (n.houseKey  IS NOT DISTINCT FROM :houseKey)
        ORDER BY n.sortOrder DESC
        """)
    List<ElementNote> findByKeys(
            @Param("signKey")    String  signKey,
            @Param("planetKey")  String  planetKey,
            @Param("houseKey")   Short   houseKey
    );

    /**
     * 取得同一組合下目前最大的 sort_order
     * 無資料時 COALESCE 回 0，確保首筆 sort_order = 1
     */
    @Query("""
        SELECT COALESCE(MAX(n.sortOrder), 0) FROM ElementNote n
        WHERE n.signKey = :signKey
          AND (n.planetKey IS NOT DISTINCT FROM :planetKey)
          AND (n.houseKey  IS NOT DISTINCT FROM :houseKey)
        """)
    Integer findMaxSortOrder(
            @Param("signKey")   String signKey,
            @Param("planetKey") String planetKey,
            @Param("houseKey")  Short  houseKey
    );

    // ── 匯出用（第二批實作，Repository 先備妥）────────────────────────────

    // 匯出全部星座解析（planet_key IS NULL）
    @Query("SELECT n FROM ElementNote n WHERE n.planetKey IS NULL ORDER BY n.signKey, n.houseKey ASC NULLS FIRST, n.sortOrder DESC")
    List<ElementNote> findAllSignNotes();

    // 匯出全部行星×星座解析（planet_key IS NOT NULL）
    @Query("SELECT n FROM ElementNote n WHERE n.planetKey IS NOT NULL ORDER BY n.planetKey, n.signKey, n.houseKey ASC NULLS FIRST, n.sortOrder DESC")
    List<ElementNote> findAllPlanetNotes();
}
