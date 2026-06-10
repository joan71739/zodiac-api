package com.project.zodiac_api.repository;

import com.project.zodiac_api.model.CodeReference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CodeReferenceRepository extends JpaRepository<CodeReference, Integer> {
    Optional<CodeReference> findByCode(String code);
    List<CodeReference> findByCategory(String category); // "planet" / "sign" / "aspect"
}
