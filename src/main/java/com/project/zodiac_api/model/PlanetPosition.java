package com.project.zodiac_api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "planet_positions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class PlanetPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "client_id", nullable = false)
    private Integer clientId;

    @Column(length = 10)
    private String planet;

    @Column(length = 10)
    private String sign;

    @Column(name = "degree_num")
    private Short degreeNum;

    @Column(name = "minute_num")
    private Short minuteNum;

    @Column(name = "house")
    private Integer house;

    @Column(length = 200)
    private String notes;

    // 是否為命主星（同一客戶同時只有一列為 TRUE）
    @Column(name = "is_lord", nullable = false)
    private Boolean isLord = false;
}
