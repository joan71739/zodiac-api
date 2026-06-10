package com.project.zodiac_api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "code_references")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class CodeReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @Column(nullable = false, length = 20)
    private String category;  // planet / sign / aspect

    @Column(name = "zh_name", nullable = false, length = 50)
    private String zhName;
}