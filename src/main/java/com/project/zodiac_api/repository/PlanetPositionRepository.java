package com.project.zodiac_api.repository;

import com.project.zodiac_api.model.PlanetPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanetPositionRepository extends JpaRepository<PlanetPosition, Integer> {

    List<PlanetPosition> findByClientId(Integer clientId);

    void deleteByClientId(Integer clientId);

    @Query("""
        SELECT p FROM PlanetPosition p
        WHERE p.planet = :planet
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
