package com.project.zodiac_api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "aspects")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Aspect {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "client_id", nullable = false)
    private Integer clientId;

    @Column(length = 50)
    private String planet1;

    @Column(name = "aspect_type", length = 30)
    private String aspectType;

    @Column(length = 50)
    private String planet2;

    @Column(precision = 4, scale = 2)
    private BigDecimal orb;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
