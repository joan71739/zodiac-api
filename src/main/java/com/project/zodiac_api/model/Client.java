package com.project.zodiac_api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "clients")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "birth_time")
    private LocalTime birthTime;

    @Column(name = "birth_place", length = 200)
    private String birthPlace;

    @Column(name = "chart_image_path", length = 500)
    private String chartImagePath;

    // ── v9：上升 / 天頂四軸資訊（允許 NULL）─────────────────────────────
    @Column(name = "asc_sign", length = 50)
    private String ascSign;
 
    @Column(name = "asc_degree_num")
    private Short ascDegreeNum;
 
    @Column(name = "asc_minute_num")
    private Short ascMinuteNum;
 
    @Column(name = "mc_sign", length = 50)
    private String mcSign;
 
    @Column(name = "mc_degree_num")
    private Short mcDegreeNum;
 
    @Column(name = "mc_minute_num")
    private Short mcMinuteNum;
    // ─────────────────────────────────────────────────────────────────────

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
