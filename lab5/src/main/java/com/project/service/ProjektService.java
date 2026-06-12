package com.project.service;

import com.project.model.Projekt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ProjektService {

    Optional<Projekt> getProjekt(Integer projektId);

    Projekt setProjekt(Projekt projekt);

    void deleteProjekt(Integer projektId);

    Page<Projekt> getProjekty(Pageable pageable);

    Page<Projekt> searchByNazwa(String nazwa, Pageable pageable);

    void addStudentToProjekt(Integer projektId, Integer studentId);

    void removeStudentFromProjekt(Integer projektId, Integer studentId);
}
