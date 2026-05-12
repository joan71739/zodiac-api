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

    @Column(length = 50)
    private String planet;

    @Column(length = 50)
    private String sign;

    @Column(name = "degree_num")
    private Short degreeNum;

    @Column(name = "minute_num")
    private Short minuteNum;

    private Integer house;

    @Column(length = 200)
    private String notes;
}
