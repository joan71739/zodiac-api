package com.project.zodiac_api.repository;

import com.project.zodiac_api.model.TransitNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransitNoteRepository extends JpaRepository<TransitNote, Integer> {

    /**
     * 查詢指定頁籤的解析列表，依 sort_order 降序（最新在最上）
     *
     * 使用 IS NOT DISTINCT FROM 處理 NULL 比對，與 ElementNoteRepository 相同原則：
     *   - aspect_type=NULL, natal_planet=NULL  → 過境宮位情境
     *   - aspect_type='q',  natal_planet='Q'   → 行運星×相位×本命星情境
     *   - transit_house=NULL                   → 詳細資料頁籤
     *   - transit_house=1                      → 過境一宮頁籤
     */
    @Query("""
        SELECT n FROM TransitNote n
        WHERE n.transitPlanet = :transitPlanet
          AND (n.aspectType   IS NOT DISTINCT FROM :aspectType)
          AND (n.natalPlanet  IS NOT DISTINCT FROM :natalPlanet)
          AND (n.transitHouse IS NOT DISTINCT FROM :transitHouse)
        ORDER BY n.sortOrder DESC
        """)
    List<TransitNote> findByKeys(
            @Param("transitPlanet") String transitPlanet,
            @Param("aspectType")    String aspectType,
            @Param("natalPlanet")   String natalPlanet,
            @Param("transitHouse")  Short  transitHouse
    );

    /**
     * 取得同一組合下目前最大的 sort_order
     * 無資料時 COALESCE 回 0，確保首筆 sort_order = 1
     */
    @Query("""
        SELECT COALESCE(MAX(n.sortOrder), 0) FROM TransitNote n
        WHERE n.transitPlanet = :transitPlanet
          AND (n.aspectType   IS NOT DISTINCT FROM :aspectType)
          AND (n.natalPlanet  IS NOT DISTINCT FROM :natalPlanet)
          AND (n.transitHouse IS NOT DISTINCT FROM :transitHouse)
        """)
    Integer findMaxSortOrder(
            @Param("transitPlanet") String transitPlanet,
            @Param("aspectType")    String aspectType,
            @Param("natalPlanet")   String natalPlanet,
            @Param("transitHouse")  Short  transitHouse
    );
}
