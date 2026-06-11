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

    // 星座代碼（a=牡羊 s=金牛 d=雙子 f=巨蟹 g=獅子 h=處女
    //           j=天秤 k=天蠍 l=射手 z=摩羯 x=水瓶 c=雙魚）
    @Column(name = "sign_key", nullable = false, length = 10)
    private String signKey;

    // 行星代碼（Q/W/E/R/T/Y/U）；NULL = 純星座解析
    @Column(name = "planet_key", length = 10)
    private String planetKey;

    // 宮位（1~12）；NULL = 星座特性頁籤
    @Column(name = "house_key")
    private Short houseKey;

    @Column(length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    // 標籤欄位（第一批先開欄位，第二批細作 UI）
    @Column(length = 200)
    private String tag;

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
