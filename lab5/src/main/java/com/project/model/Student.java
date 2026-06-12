package com.project.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Student {

    private Integer studentId;

    @NotBlank(message = "Pole imię nie może być puste!")
    @Size(max = 50, message = "Imię musi zawierać co najwyżej {max} znaków!")
    private String imie;

    @NotBlank(message = "Pole nazwisko nie może być puste!")
    @Size(max = 100, message = "Nazwisko musi zawierać co najwyżej {max} znaków!")
    private String nazwisko;

    @NotBlank(message = "Pole nr indeksu nie może być puste!")
    @Size(max = 20, message = "Nr indeksu musi zawierać co najwyżej {max} znaków!")
    private String nrIndeksu;

    @NotBlank(message = "Pole email nie może być puste!")
    @Email(message = "Pole email musi zawierać prawidłowy adres email!")
    @Size(max = 50, message = "Email musi zawierać co najwyżej {max} znaków!")
    private String email;

    @NotNull(message = "Pole stacjonarny nie może być puste!")
    private Boolean stacjonarny;
}
