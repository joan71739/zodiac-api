package com.project.zodiac_api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "backup_records")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class BackupRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    // columnDefinition 與 schema.sql 的 DEFAULT '手動備份' 對齊；
    // performBackup() 永遠顯式傳入 note，不依賴此 DEFAULT，
    // 但直接 SQL INSERT（bypass JPA）時仍可套用正確預設值。
    @Column(length = 100, columnDefinition = "VARCHAR(100) DEFAULT '手動備份'")
    private String note;

    // created_at 由 @PrePersist 填入（Java 層），與 Client / AnalysisNote 等 Entity 設計一致。
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
