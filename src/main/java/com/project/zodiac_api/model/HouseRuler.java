package com.project.zodiac_api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "house_rulers")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class HouseRuler {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "client_id", nullable = false)
    private Integer clientId;

    @Column(name = "house_number", nullable = false)
    private Integer houseNumber;

    @Column(name = "ruling_planet", length = 10)
    private String rulingPlanet;

    @Column(name = "flies_to_house")
    private Integer fliesToHouse;

    @Column(name = "flies_to_sign", length = 10)
    private String fliesToSign;

}
