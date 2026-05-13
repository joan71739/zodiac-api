package com.project.zodiac_api.controller;

import com.project.zodiac_api.exception.ResourceNotFoundException;
import com.project.zodiac_api.model.BackupRecord;
import com.project.zodiac_api.repository.BackupRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/backup")
@RequiredArgsConstructor
public class BackupController {

    private final BackupRecordRepository backupRepo;

    @Value("${app.backup.dir}")
    private String backupDir;

    @Value("${app.backup.db-name}")
    private String dbName;

    @Value("${app.backup.db-host}")
    private String dbHost;

    @Value("${app.backup.db-port}")
    private String dbPort;

    @Value("${app.backup.db-user}")
    private String dbUser;

    @Value("${app.backup.pg-dump-path:pg_dump}")
    private String pgDumpPath;

    @Value("${app.backup.psql-path:psql}")
    private String psqlPath;

    // ── API ─────────────────────────────────────────────

    // POST /api/backup  （手動觸發）
    @PostMapping
    public ResponseEntity<Map<String, Object>> backup() throws IOException, InterruptedException {
        BackupRecord record = performBackup("手動備份");
        return ResponseEntity.ok(toMap(record));
    }

    // GET /api/backup/list
    @GetMapping("/list")
    public ResponseEntity<List<Map<String, Object>>> list() {
        List<Map<String, Object>> result = backupRepo.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toMap)
                .toList();
        return ResponseEntity.ok(result);
    }

    // POST /api/backup/restore  body: { "backupId": 1 }
    @PostMapping("/restore")
    public ResponseEntity<Map<String, Object>> restore(@RequestBody Map<String, Integer> body)
            throws IOException, InterruptedException {

        Integer backupId = body.get("backupId");
        if (backupId == null) throw new IllegalArgumentException("backupId 不得為空");

        BackupRecord record = backupRepo.findById(backupId)
                .orElseThrow(() -> new ResourceNotFoundException("BackupRecord", backupId));

        Path filePath = Paths.get(record.getFilePath());
        if (!Files.exists(filePath)) {
            throw new ResourceNotFoundException("備份檔案不存在: " + record.getFilePath());
        }

        // psql 還原
        ProcessBuilder pb = new ProcessBuilder(
                psqlPath,
                "-h", dbHost,
                "-p", dbPort,
                "-U", dbUser,
                "-d", dbName,
                "-f", filePath.toAbsolutePath().toString()
        );
        pb.environment().put("PGPASSWORD", System.getenv("DB_PASSWORD") != null
                ? System.getenv("DB_PASSWORD") : "");
        pb.redirectErrorStream(true);
        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("還原失敗，psql exit code: " + exitCode);
        }

        return ResponseEntity.ok(Map.of(
                "message", "還原成功",
                "backupId", backupId,
                "restoredAt", LocalDateTime.now().toString()
        ));
    }

    // ── 排程：每天凌晨 03:00 自動備份 ──────────────────

    @Scheduled(cron = "0 0 3 * * *")
    public void scheduledBackup() {
        try {
            performBackup("自動備份");
            log.info("[Backup] 自動備份完成");
            cleanOldBackups();
        } catch (Exception e) {
            log.error("[Backup] 自動備份失敗: {}", e.getMessage());
        }
    }

    // ── 私有方法 ─────────────────────────────────────────

    private BackupRecord performBackup(String note) throws IOException, InterruptedException {
        Path dir = Paths.get(backupDir);
        if (!Files.exists(dir)) Files.createDirectories(dir);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "backup_" + timestamp + ".sql";
        Path dest = dir.resolve(filename);

        // pg_dump 備份
        ProcessBuilder pb = new ProcessBuilder(
                pgDumpPath,
                "-h", dbHost,
                "-p", dbPort,
                "-U", dbUser,
                "-d", dbName,
                "-f", dest.toAbsolutePath().toString()
        );
        pb.environment().put("PGPASSWORD", System.getenv("DB_PASSWORD") != null
                ? System.getenv("DB_PASSWORD") : "");
        pb.redirectErrorStream(true);
        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("pg_dump 執行失敗，exit code: " + exitCode);
        }

        BackupRecord record = new BackupRecord();
        record.setFilePath(dest.toAbsolutePath().toString());
        record.setNote(note);
        return backupRepo.save(record);
    }

    /**
     * 清除 30 天前的備份（實體檔 + DB 紀錄）
     */
    private void cleanOldBackups() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        List<BackupRecord> old = backupRepo.findOlderThan(cutoff);
        for (BackupRecord r : old) {
            try {
                Files.deleteIfExists(Paths.get(r.getFilePath()));
            } catch (IOException e) {
                log.warn("[Backup] 刪除舊備份檔案失敗: {}", r.getFilePath());
            }
        }
        backupRepo.deleteAll(old);
        log.info("[Backup] 清除 {} 筆過期備份", old.size());
    }

    private Map<String, Object> toMap(BackupRecord r) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", r.getId());
        map.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().toString() : null);
        map.put("note", r.getNote());
        return map;
    }
}
