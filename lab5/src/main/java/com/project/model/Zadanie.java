package com.project.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Zadanie {

    private Integer zadanieId;

    @NotBlank(message = "Pole nazwa nie może być puste!")
    @Size(min = 1, max = 50, message = "Nazwa musi zawierać od {min} do {max} znaków!")
    private String nazwa;

    private Integer kolejnosc;

    @Size(max = 1000, message = "Pole opis może zawierać maksymalnie {max} znaków!")
    private String opis;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime dataczasDodania;

    @JsonIgnoreProperties({"projekt"})
    private Projekt projekt;
}
