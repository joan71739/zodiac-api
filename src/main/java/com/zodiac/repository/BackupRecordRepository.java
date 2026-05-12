package com.zodiac.repository;

import com.zodiac.model.BackupRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BackupRecordRepository extends JpaRepository<BackupRecord, Integer> {

    List<BackupRecord> findAllByOrderByCreatedAtDesc();

    /** 取得 30 天前的備份紀錄，用於自動清除 */
    @Query("SELECT b FROM BackupRecord b WHERE b.createdAt < :cutoff")
    List<BackupRecord> findOlderThan(LocalDateTime cutoff);
}
