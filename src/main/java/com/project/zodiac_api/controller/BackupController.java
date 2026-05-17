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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@RestController
@RequestMapping("/api/backup")
@RequiredArgsConstructor
public class BackupController {

    private final BackupRecordRepository backupRepo;

    /**
     * [Fix #1] 防止手動備份重複觸發。
     *
     * 原始實作使用 new Thread().start()，無任何 lock 機制，
     * 使用者快速連點「立即備份」會同時啟動多個 pg_dump process：
     *   (a) DB 與 I/O 壓力同步飆高
     *   (b) timestamp 精度到秒，同一秒內兩次備份的檔名相同，
     *       後者覆蓋前者的 .sql，但 DB 會留下兩筆指向同一檔案的紀錄。
     *
     * 以 AtomicBoolean 作為輕量 lock：
     *   - compareAndSet(false, true) 保證只有第一個請求成功進入
     *   - finally 區塊確保備份結束後（成功或失敗）都會釋放 lock
     *   - 重複請求立即回傳 HTTP 409，前端可據此顯示適當提示
     */
    private final AtomicBoolean isBackingUp = new AtomicBoolean(false);

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

    // 預設值為相對路徑 pg_dump / psql，依賴 PATH 環境變數
    // Docker 部署時請在 application.properties 改為完整路徑（如 /usr/bin/pg_dump）
    @Value("${app.backup.pg-dump-path:pg_dump}")
    private String pgDumpPath;

    @Value("${app.backup.psql-path:psql}")
    private String psqlPath;

    // ⚠️ 必填：空字串會導致 PGPASSWORD 環境變數為空，pg_dump / psql 可能要求互動式輸入而 hang 住
    @Value("${app.backup.db-password}")
    private String dbPassword;

    // ── API ─────────────────────────────────────────────

    /**
     * POST /api/backup — 手動觸發備份（非同步）
     *
     * 使用 AtomicBoolean 防止重複觸發（見 Fix #1 說明）。
     * 備份在背景 Thread 執行，API 立即回傳，前端應等待後再重新整理列表。
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> backup() {
        if (!isBackingUp.compareAndSet(false, true)) {
            return ResponseEntity.status(409)
                    .body(Map.of("message", "備份進行中，請稍後再試"));
        }
        new Thread(() -> {
            try {
                performBackup("手動備份");
                log.info("[Backup] 手動備份完成");
            } catch (Exception e) {
                log.error("[Backup] 手動備份失敗: {}", e.getMessage());
            } finally {
                // 無論成功或失敗都必須釋放 lock，否則後續備份永遠被擋
                isBackingUp.set(false);
            }
        }).start();
        return ResponseEntity.ok(Map.of("message", "備份已啟動，請稍後重新整理列表"));
    }

    // GET /api/backup/list — 備份歷史（依建立時間降序）
    @GetMapping("/list")
    public ResponseEntity<List<Map<String, Object>>> list() {
        List<Map<String, Object>> result = backupRepo.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toMap)
                .toList();
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/backup/restore — 還原備份
     * Request Body: { "backupId": N }
     *
     * [Fix #2] 原始實作：psql Process 啟動後未消費 stdout/stderr stream，
     * 直接呼叫 process.waitFor()。
     *
     * psql 還原時會輸出大量 ALTER TABLE / CREATE INDEX 等訊息（經 redirectErrorStream 合併至 stdout）。
     * 若 subprocess 的 pipe buffer（通常 64KB）被填滿，psql 會 block 在寫 output，
     * 而 Java 這邊同時在等 waitFor()，雙方互等造成 deadlock，還原操作永久 hang 住。
     *
     * 修正：仿照 performBackup()，在 waitFor() 前以 BufferedReader 完整消費 InputStream。
     * exitCode != 0 時將 output 印至 log 方便排查原因。
     */
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

        ProcessBuilder pb = new ProcessBuilder(
                psqlPath,
                "-h", dbHost,
                "-p", dbPort,
                "-U", dbUser,
                "-d", dbName,
                "-f", filePath.toAbsolutePath().toString()
        );
        pb.environment().put("PGPASSWORD", dbPassword);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        // [Fix #2] 消費 stdout/stderr stream，避免 buffer 塞住造成 deadlock
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            log.error("[Backup] psql 還原失敗 (exit code: {})，輸出如下:\n{}", exitCode, output);
            throw new RuntimeException("還原失敗，psql exit code: " + exitCode);
        }

        return ResponseEntity.ok(Map.of(
                "message", "還原成功",
                "backupId", backupId,
                "restoredAt", LocalDateTime.now().toString()
        ));
    }

    // ── 排程：每天凌晨 03:00 自動備份 ──────────────────
    // @EnableScheduling 已在 ZodiacApiApplication.java 啟用

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

        ProcessBuilder pb = new ProcessBuilder(
                pgDumpPath,
                "-h", dbHost,
                "-p", dbPort,
                "-U", dbUser,
                "-d", dbName,
                "-f", dest.toAbsolutePath().toString()
        );
        pb.environment().put("PGPASSWORD", dbPassword);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        // 讀取 pg_dump 的 stdout/stderr，避免 buffer 塞住（pg_dump 以 -f 輸出至檔案，stdout 主要為 stderr 訊息）
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            log.error("[Backup] pg_dump 失敗 (exit code: {})，輸出如下:\n{}", exitCode, output);
            throw new RuntimeException("pg_dump 執行失敗，exit code: " + exitCode);
        }

        BackupRecord record = new BackupRecord();
        record.setFilePath(dest.toAbsolutePath().toString());
        record.setNote(note);
        return backupRepo.save(record);
    }

    /**
     * 清除 30 天前的備份（實體檔 + DB 紀錄）
     *
     * [Fix #3] 原始實作：Files.deleteIfExists() 拋出 IOException 時只 log.warn，
     * 後續 backupRepo.deleteAll(old) 仍全數執行。
     * 結果：DB 紀錄被刪，但實體 .sql 檔仍在磁碟，變成無法透過 API 管理的殭屍檔。
     *
     * 修正：改為「刪檔成功後才加入 toDelete 清單」，最後只對成功移除實體檔的紀錄執行 deleteAll。
     * 刪檔失敗的紀錄保留在 DB，下次排程會再次嘗試清除。
     */
    private void cleanOldBackups() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        List<BackupRecord> old = backupRepo.findOlderThan(cutoff);
        List<BackupRecord> toDelete = new ArrayList<>();

        for (BackupRecord r : old) {
            try {
                Files.deleteIfExists(Paths.get(r.getFilePath()));
                toDelete.add(r);  // 只有實體檔成功刪除後才加入
            } catch (IOException e) {
                // 實體檔刪除失敗 → 保留 DB 紀錄，避免殭屍檔案
                log.warn("[Backup] 刪除舊備份檔案失敗，保留 DB 紀錄，下次排程重試: {}", r.getFilePath());
            }
        }

        backupRepo.deleteAll(toDelete);
        log.info("[Backup] 清除 {}/{} 筆過期備份（其餘因刪檔失敗保留紀錄）",
                toDelete.size(), old.size());
    }

    /**
     * 回傳備份清單時不暴露 filePath 給前端（安全考量：避免洩漏伺服器目錄結構）
     */
    private Map<String, Object> toMap(BackupRecord r) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", r.getId());
        map.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().toString() : null);
        map.put("note", r.getNote());
        return map;
    }
}