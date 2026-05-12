package com.zodiac.repository;

import com.zodiac.model.HouseRuler;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HouseRulerRepository extends JpaRepository<HouseRuler, Integer> {

    List<HouseRuler> findByClientIdOrderByHouseNumberAsc(Integer clientId);

    Optional<HouseRuler> findByClientIdAndId(Integer clientId, Integer id);

    void deleteByClientId(Integer clientId);
}
