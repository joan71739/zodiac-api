package com.project.zodiac_api.repository;

import com.project.zodiac_api.model.ChartPreferences;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * chart_preferences JPA Repository。
 * 只會用到繼承來的 findById(1) + save()，不需自訂方法。
 */
public interface ChartPreferencesRepository extends JpaRepository<ChartPreferences, Integer> {
}
