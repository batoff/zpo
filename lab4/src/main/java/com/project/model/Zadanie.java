package com.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "zadanie")
public class Zadanie {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "zadanie_seq")
    @SequenceGenerator(name = "zadanie_seq", sequenceName = "zadanie_seq", allocationSize = 1, initialValue = 1)
    @Column(name = "zadanie_id")
    private Integer zadanieId;

    @ManyToOne
    @JoinColumn(name = "projekt_id")
    private Projekt projekt;

    @NotBlank(message = "Pole nazwa nie może być puste!")
    @Size(min = 1, max = 50, message = "Nazwa musi zawierać od {min} do {max} znaków!")
    @Column(nullable = false, length = 50)
    private String nazwa;

    @Column
    private Integer kolejnosc;

    @Column(length = 1000)
    private String opis;

    @CreatedDate
    @Column(name = "dataczas_dodania", nullable = false, updatable = false)
    private LocalDateTime dataczasDodania;
}
