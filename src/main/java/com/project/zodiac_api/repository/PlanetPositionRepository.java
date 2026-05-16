package com.project.zodiac_api.repository;

import com.project.zodiac_api.model.PlanetPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanetPositionRepository extends JpaRepository<PlanetPosition, Integer> {

    List<PlanetPosition> findByClientId(Integer clientId);

    void deleteByClientId(Integer clientId);

    /**
     * v8 BUG FIX：清除同一 client 所有行星的 is_lord 旗標。
     * 在 update() 設定 isLord=true 之前呼叫，確保全局唯一性。
     * 需搭配 @Transactional 使用。
     */
    @Modifying
    @Query("UPDATE PlanetPosition p SET p.isLord = false WHERE p.clientId = :clientId")
    void clearLordByClientId(@Param("clientId") Integer clientId);

    /**
     * 篩選客戶用查詢。
     *
     * v8 命主星特殊處理：
     *   - planet = '命主星' → 改查 is_lord = TRUE（命主星已無獨立列）
     *   - 其他行星         → 正常比對 planet 欄位
     */
    @Query("""
        SELECT p FROM PlanetPosition p
        WHERE (
            (:planet <> '命主星' AND p.planet = :planet)
            OR
            (:planet = '命主星' AND p.isLord = TRUE)
        )
        AND p.sign = :sign
        AND (:degreeFrom IS NULL OR p.degreeNum >= :degreeFrom)
        AND (:degreeTo   IS NULL OR p.degreeNum <= :degreeTo)
        AND (:house      IS NULL OR p.house = :house)
        """)
    List<PlanetPosition> search(
        @Param("planet")     String planet,
        @Param("sign")       String sign,
        @Param("degreeFrom") Short degreeFrom,
        @Param("degreeTo")   Short degreeTo,
        @Param("house")      Integer house
    );
}
