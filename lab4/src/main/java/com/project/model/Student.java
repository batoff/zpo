package com.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "student",
        indexes = {
                @Index(name = "idx_nazwisko", columnList = "nazwisko", unique = false),
                @Index(name = "idx_nr_indeksu", columnList = "nr_indeksu", unique = true)
        })
public class Student {

    @Id
    @GeneratedValue
    @Column(name = "student_id")
    private Integer studentId;

    @NotBlank(message = "Pole imię nie może być puste!")
    @Size(max = 50, message = "Imię musi zawierać co najwyżej {max} znaków!")
    @Column(nullable = false, length = 50)
    private String imie;

    @NotBlank(message = "Pole nazwisko nie może być puste!")
    @Size(max = 100, message = "Nazwisko musi zawierać co najwyżej {max} znaków!")
    @Column(nullable = false, length = 100)
    private String nazwisko;

    @NotBlank(message = "Pole nr indeksu nie może być puste!")
    @Size(max = 20, message = "Nr indeksu musi zawierać co najwyżej {max} znaków!")
    @Column(name = "nr_indeksu", nullable = false, unique = true, length = 20)
    private String nrIndeksu;

    @NotBlank(message = "Pole email nie może być puste!")
    @Email(message = "Pole email musi zawierać prawidłowy adres email!")
    @Size(max = 50, message = "Email musi zawierać co najwyżej {max} znaków!")
    @Column(nullable = false, unique = true, length = 50)
    private String email;

    @NotNull(message = "Pole stacjonarny nie może być puste!")
    @Column(nullable = false)
    private Boolean stacjonarny;

    @ManyToMany(mappedBy = "studenci")
    private Set<Projekt> projekty;
}
