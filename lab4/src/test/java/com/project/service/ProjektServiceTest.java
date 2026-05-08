package com.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.project.model.Projekt;
import com.project.model.Zadanie;
import com.project.repository.ProjektRepository;
import com.project.repository.ZadanieRepository;

/**
 * Klasa ProjektServiceTest jest przykładem TESTU JEDNOSTKOWEGO warstwy serwisu. <br/>
 * Zauważ różnice względem testów kontrolera: <br/>
 * 1. To jest typowy test Mockito - nie potrzebuje Springa, co czyni go bardzo szybkim.<br/>
 * 2. Test sprawdza warstwę serwisu, tzw. LOGIKĘ BIZNESOWĄ. <br/>
 * 3. W tym przypadku test jednostkowy jest wskazany – serwis musi działać poprawnie <br/>
 * niezależnie od tego, czy dane przychodzą z API, czy z innego modułu. <br/>
 */
@ExtendWith(MockitoExtension.class)
public class ProjektServiceTest {

    @Mock
    private ProjektRepository mockProjektRepository;

    @Mock
    private ZadanieRepository mockZadanieRepository;

    @InjectMocks
    private ProjektServiceImpl projektService;

    @Test
    void getProjekt_whenValidId_returnsProjekt() {
        // GIVEN
        Integer projektId = 1;
        Projekt expectedProjekt = createProjektTestowy(projektId, "Nazwa testowa");
        given(mockProjektRepository.findById(projektId)).willReturn(Optional.of(expectedProjekt));

        // WHEN
        Optional<Projekt> actualProjekt = projektService.getProjekt(projektId);

        // THEN
        assertThat(actualProjekt).isPresent().contains(expectedProjekt);
        verify(mockProjektRepository).findById(projektId);
    }

    @Test
    void setProjekt_whenCalled_savesAndReturnsProjekt() {
        // GIVEN
        Projekt projektToSave = createProjektTestowy(null, "Nazwa testowa");
        Integer projektId = 1;
        Projekt savedProjekt = createProjektTestowy(projektId, projektToSave.getNazwa());
        given(mockProjektRepository.save(projektToSave)).willReturn(savedProjekt);

        // WHEN
        Projekt actualProjekt = projektService.setProjekt(projektToSave);

        // THEN
        assertThat(actualProjekt).isEqualTo(savedProjekt);
        verify(mockProjektRepository).save(projektToSave);
    }

    @Test
    void deleteProjekt_whenValidId_deletesTasksAndProjekt() {
        // GIVEN
        Integer projektId = 1;
        List<Zadanie> powiazaneZadania = List.of(
                createZadanieTestowe(1, "Zadanie 1"),
                createZadanieTestowe(2, "Zadanie 2")
        );
        given(mockZadanieRepository.findZadaniaProjektu(projektId)).willReturn(powiazaneZadania);

        // WHEN
        projektService.deleteProjekt(projektId);

        // THEN
        verify(mockZadanieRepository, times(1)).delete(powiazaneZadania.get(0));
        verify(mockZadanieRepository, times(1)).delete(powiazaneZadania.get(1));
        verify(mockProjektRepository).deleteById(projektId);
    }

    @Test
    void getProjekty_whenCalled_returnsPageFromRepository() {
        // GIVEN
        List<Projekt> listaProjektow = List.of(
                createProjektTestowy(1, "Nazwa testowa 1"),
                createProjektTestowy(2, "Nazwa testowa 2"),
                createProjektTestowy(3, "Nazwa testowa 3")
        );
        Pageable pageable = PageRequest.of(0, 10);
        Page<Projekt> expectedPage = new PageImpl<>(listaProjektow);
        given(mockProjektRepository.findAll(pageable)).willReturn(expectedPage);

        // WHEN
        Page<Projekt> actualPage = projektService.getProjekty(pageable);

        // THEN
        assertThat(actualPage).isEqualTo(expectedPage);
        verify(mockProjektRepository).findAll(pageable);
    }

    @Test
    void searchByNazwa_whenPhraseProvided_callsRepositoryWithCorrectParams() {
        // GIVEN
        String searchPhrase = "java";
        Pageable pageable = PageRequest.of(0, 5);
        given(mockProjektRepository.findByNazwaContainingIgnoreCase(any(), any())).willReturn(Page.empty());

        // WHEN
        projektService.searchByNazwa(searchPhrase, pageable);

        // THEN
        verify(mockProjektRepository).findByNazwaContainingIgnoreCase(searchPhrase, pageable);
    }

    // --- metody pomocnicze
    private Projekt createProjektTestowy(Integer id, String nazwa) {
        return Projekt.builder()
                .projektId(id)
                .nazwa(nazwa)
                .opis("Opis testowy")
                .dataOddania(LocalDate.of(2026, 6, 1)).build();
    }

    private Zadanie createZadanieTestowe(Integer id, String nazwa) {
        return Zadanie.builder()
                .zadanieId(id)
                .nazwa(nazwa)
                .build();
    }
}
