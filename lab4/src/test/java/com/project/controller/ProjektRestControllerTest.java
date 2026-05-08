package com.project.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.project.model.Projekt;
import com.project.service.ProjektService;

@ExtendWith(MockitoExtension.class)
public class ProjektRestControllerTest {

    @Mock
    private ProjektService mockProjektService;

    @InjectMocks
    private ProjektRestController projectRestController;

    @Test
    void getProjekt_whenValidId_returnsProjekt() {
        // GIVEN
        Integer projektId = 1;
        Projekt expectedProjekt = createProjektTestowy(projektId, "Nazwa testowa");
        given(mockProjektService.getProjekt(projektId)).willReturn(Optional.of(expectedProjekt));

        // WHEN
        ResponseEntity<Projekt> responseEntity = projectRestController.getProjekt(projektId);

        // THEN
        assertAll(
                () -> assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(responseEntity.getBody()).isEqualTo(expectedProjekt));
    }

    @Test
    void getProjekt_whenInvalidId_returnsNotFound() {
        // GIVEN
        Integer projektId = 1;
        given(mockProjektService.getProjekt(projektId)).willReturn(Optional.empty());

        // WHEN
        ResponseEntity<Projekt> responseEntity = projectRestController.getProjekt(projektId);

        // THEN
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getProjekty_returnsPageWithProjekty() {
        // GIVEN
        List<Projekt> listaProjektow = List.of(
                createProjektTestowy(1, "Nazwa testowa 1"),
                createProjektTestowy(2, "Nazwa testowa 2"),
                createProjektTestowy(3, "Nazwa testowa 3"));
        PageRequest pageable = PageRequest.of(1, 5);
        Page<Projekt> page = new PageImpl<>(listaProjektow, pageable, 5);
        given(mockProjektService.getProjekty(pageable)).willReturn(page);

        // WHEN
        Page<Projekt> pageWithProjects = projectRestController.getProjekty(pageable);

        // THEN
        assertThat(pageWithProjects).isNotNull();
        assertThat(pageWithProjects.getContent())
                .isNotNull()
                .hasSize(3)
                .containsExactlyInAnyOrderElementsOf(listaProjektow);
    }

    @Test
    void createProjekt_whenValidData_returnsCreatedWithLocation() {
        // GIVEN
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        Projekt projektToSave = createProjektTestowy(null, "Nazwa testowa");
        Integer projektId = 1;
        Projekt createdProjekt = createProjektTestowy(projektId, projektToSave.getNazwa());
        given(mockProjektService.setProjekt(projektToSave)).willReturn(createdProjekt);

        // WHEN
        ResponseEntity<Void> responseEntity = projectRestController.createProjekt(projektToSave);

        // THEN
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getHeaders().getLocation().getPath()).isEqualTo("/" + projektId);

        verify(mockProjektService).setProjekt(projektToSave);
    }

    @Test
    void updateProjekt_whenValidData_returnsOk() {
        // GIVEN
        Integer projektId = 1;
        Projekt projektToUpdate = createProjektTestowy(projektId, "Stara nazwa");
        Projekt updatedProjekt = createProjektTestowy(projektId, "Nowa nazwa");
        given(mockProjektService.getProjekt(projektId)).willReturn(Optional.of(projektToUpdate));

        // WHEN
        ResponseEntity<Void> responseEntity = projectRestController.updateProjekt(updatedProjekt, projektId);

        // THEN
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(mockProjektService).setProjekt(updatedProjekt);
    }

    @Test
    void deleteProjekt_whenValidId_returnsOk() {
        // GIVEN
        Integer projektId = 1;
        Projekt projektToDelete = createProjektTestowy(projektId, "Nazwa testowa");
        given(mockProjektService.getProjekt(projektId)).willReturn(Optional.of(projektToDelete));

        // WHEN
        ResponseEntity<Void> responseEntity = projectRestController.deleteProjekt(projektId);

        // THEN
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(mockProjektService).deleteProjekt(projektId);
    }

    @Test
    void deleteProjekt_whenInvalidId_returnsNotFound() {
        // GIVEN
        Integer projektId = 1;

        // WHEN
        ResponseEntity<Void> responseEntity = projectRestController.deleteProjekt(projektId);

        // THEN
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(mockProjektService, never()).deleteProjekt(any());
    }

    private Projekt createProjektTestowy(Integer id, String nazwa) {
        return Projekt.builder()
                .projektId(id)
                .nazwa(nazwa)
                .opis("Opis testowy")
                .dataOddania(LocalDate.of(2026, 6, 1))
                .build();
    }

    @AfterEach
    void resetRequestAttributes() {
        RequestContextHolder.resetRequestAttributes();
    }
}
