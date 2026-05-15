package com.project.zodiac_api.dto;
 
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
 
import java.time.LocalDate;
import java.time.LocalTime;
 
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ClientRequestDto {
 
    @NotBlank(message = "姓名不得為空")
    private String name;
 
    private LocalDate birthDate;
    private LocalTime birthTime;
    private String birthPlace;

 // ── v9：上升 / 天頂四軸資訊（選填，允許 null）──────────────────────
    private String ascSign;
    private Short  ascDegreeNum;
    private Short  ascMinuteNum;
 
    private String mcSign;
    private Short  mcDegreeNum;
    private Short  mcMinuteNum;
    // ─────────────────────────────────────────────────────────────────────
}
 
