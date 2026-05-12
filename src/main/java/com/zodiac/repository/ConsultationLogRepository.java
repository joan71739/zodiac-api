package com.zodiac.repository;

import com.zodiac.model.ConsultationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultationLogRepository extends JpaRepository<ConsultationLog, Integer> {

    List<ConsultationLog> findByClientIdOrderByConsultationDateDesc(Integer clientId);

    Optional<ConsultationLog> findByClientIdAndId(Integer clientId, Integer id);
}
