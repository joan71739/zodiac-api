package com.project.zodiac_api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "element_notes")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ElementNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "sign_key", nullable = false, length = 10)
    private String signKey;

    // NULL = 純星座解析；NOT NULL = 行星×星座解析
    @Column(name = "planet_key", length = 10)
    private String planetKey;

    // NULL = 星座特性頁籤；1~12 = 宮位頁籤
    @Column(name = "house_key")
    private Short houseKey;

    @Column(length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    // 標籤（手動輸入，第二批細作 UI）
    @Column(length = 200)
    private String tag;

    // 主題分類：general / career / love / wealth / challenge；NULL = 未分類
    @Column(length = 20)
    private String topic;

    // 數字越大越新，最新在最上
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
