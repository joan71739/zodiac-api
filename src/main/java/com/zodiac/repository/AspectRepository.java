package com.zodiac.repository;

import com.zodiac.model.Aspect;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AspectRepository extends JpaRepository<Aspect, Integer> {

    List<Aspect> findByClientId(Integer clientId);

    Optional<Aspect> findByClientIdAndId(Integer clientId, Integer id);

    void deleteByClientId(Integer clientId);
}
