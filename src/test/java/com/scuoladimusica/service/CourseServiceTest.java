package com.scuoladimusica.service;

import com.scuoladimusica.TestDataFactory;
import com.scuoladimusica.exception.BusinessRuleException;
import com.scuoladimusica.exception.DuplicateResourceException;
import com.scuoladimusica.exception.ResourceNotFoundException;
import com.scuoladimusica.model.dto.request.CourseRequest;
import com.scuoladimusica.model.dto.request.LessonRequest;
import com.scuoladimusica.model.dto.response.CourseResponse;
import com.scuoladimusica.model.dto.response.LessonResponse;
import com.scuoladimusica.model.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CourseServiceTest {

    @Autowired
    private CourseService courseService;

    @Autowired
    private TestDataFactory dati;

    // ================================================================
    // CREAZIONE CORSO
    // ================================================================

    @Nested
    @DisplayName("Creazione Corso")
    class CreazioneCorso {

        @Test
        @DisplayName("Crea corso in presenza con tutti i campi - verifica tutti i campi della response")
        void creaCorso_tuttiICampi_successo() {
            CourseRequest request = new CourseRequest(
                    "C001", "Pianoforte Base",
                    LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30),
                    25.0, 40, false, Livello.PRINCIPIANTE);

            CourseResponse response = courseService.createCourse(request);

            assertAll("Verifica tutti i campi della response",
                    () -> assertNotNull(response.id(), "L'ID deve essere generato automaticamente"),
                    () -> assertEquals("C001", response.codiceCorso()),
                    () -> assertEquals("Pianoforte Base", response.nome()),
                    () -> assertEquals(LocalDate.of(2026, 3, 1), response.dataInizio()),
                    () -> assertEquals(LocalDate.of(2026, 6, 30), response.dataFine()),
                    () -> assertEquals(25.0, response.costoOrario()),
                    () -> assertEquals(40, response.totaleOre()),
                    () -> assertFalse(response.online()),
                    () -> assertEquals(Livello.PRINCIPIANTE, response.livello()),
                    () -> assertNull(response.nomeInsegnante(), "Senza insegnante assegnato deve essere null"),
                    () -> assertEquals(0, response.numeroIscritti())
            );
        }

        @Test
        @DisplayName("Crea corso online - il flag online deve essere true")
        void creaCorso_online() {
            CourseRequest request = new CourseRequest(
                    "C001", "Chitarra Online",
                    LocalDate.of(2026, 4, 1), LocalDate.of(2026, 7, 31),
                    20.0, 30, true, Livello.INTERMEDIO);

            CourseResponse response = courseService.createCourse(request);

            assertTrue(response.online(), "Il corso deve essere online");
            assertEquals(Livello.INTERMEDIO, response.livello());
        }

        @Test
        @DisplayName("Crea corso senza livello - deve usare PRINCIPIANTE come default")
        void creaCorso_senzaLivello_defaultPrincipiante() {
            CourseRequest request = new CourseRequest(
                    "C001", "Test Corso",
                    LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30),
                    25.0, 40, false, null);

            CourseResponse response = courseService.createCourse(request);

            assertEquals(Livello.PRINCIPIANTE, response.livello(),
                    "Senza livello specificato deve essere PRINCIPIANTE");
        }

        @Test
        @DisplayName("Crea corso con codice duplicato - deve lanciare DuplicateResourceException")
        void creaCorso_codiceDuplicato_errore() {
            Teacher insegnante = dati.creaInsegnantePredefinito();
            dati.creaCorsoPredefinito(insegnante);

            CourseRequest request = new CourseRequest(
                    "C001", "Altro Corso",
                    LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30),
                    25.0, 40, false, null);

            assertThrows(DuplicateResourceException.class,
                    () -> courseService.createCourse(request),
                    "Codice duplicato deve lanciare DuplicateResourceException");
        }

        @Test
        @DisplayName("Crea corso con dataFine <= dataInizio - deve lanciare BusinessRuleException")
        void creaCorso_dateInvalide_errore() {
            CourseRequest request = new CourseRequest(
                    "C001", "Corso",
                    LocalDate.of(2026, 6, 30), LocalDate.of(2026, 3, 1),
                    25.0, 40, false, null);

            assertThrows(BusinessRuleException.class,
                    () -> courseService.createCourse(request),
                    "DataFine <= DataInizio deve lanciare BusinessRuleException");
        }

        @Test
        @DisplayName("Crea corso con dataFine uguale a dataInizio - deve lanciare BusinessRuleException")
        void creaCorso_dateUguali_errore() {
            CourseRequest request = new CourseRequest(
                    "C001", "Corso",
                    LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 1),
                    25.0, 40, false, null);

            assertThrows(BusinessRuleException.class,
                    () -> courseService.createCourse(request),
                    "DataFine uguale a DataInizio deve lanciare BusinessRuleException");
        }

        @Test
        @DisplayName("Verifica che il costoTotale sia calcolato come costoOrario * totaleOre")
        void creaCorso_verificaCostoTotale() {
            CourseRequest request = new CourseRequest(
                    "C001", "Pianoforte Base",
                    LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30),
                    25.0, 40, false, Livello.PRINCIPIANTE);

            CourseResponse response = courseService.createCourse(request);

            assertEquals(25.0 * 40, response.costoTotale(),
                    "Il costoTotale deve essere costoOrario * totaleOre");
        }

        @Test
        @DisplayName("Verifica che la durataGiorni sia calcolata correttamente")
        void creaCorso_verificaDurataGiorni() {
            CourseRequest request = new CourseRequest(
                    "C001", "Pianoforte Base",
                    LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30),
                    25.0, 40, false, Livello.PRINCIPIANTE);

            CourseResponse response = courseService.createCourse(request);

            assertTrue(response.durataGiorni() > 0,
                    "La durataGiorni deve essere positiva");
        }
    }

    // ================================================================
    // RICERCA CORSI
    // ================================================================

    @Nested
    @DisplayName("Ricerca Corsi")
    class RicercaCorsi {

        @Test
        @DisplayName("Trova corso per codice - verifica dati corretti")
        void trovaPerCodice_trovato() {
            Teacher insegnante = dati.creaInsegnantePredefinito();
            dati.creaCorsoPredefinito(insegnante);

            CourseResponse response = courseService.getCourseByCode("C001");

            assertEquals("C001", response.codiceCorso());
            assertEquals("Pianoforte Base", response.nome());
        }

        @Test
        @DisplayName("Trova corso per codice inesistente - deve lanciare ResourceNotFoundException")
        void trovaPerCodice_nonTrovato() {
            assertThrows(ResourceNotFoundException.class,
                    () -> courseService.getCourseByCode("INESISTENTE"),
                    "Codice inesistente deve lanciare ResourceNotFoundException");
        }

        @Test
        @DisplayName("Recupera tutti i corsi - lista con 2 elementi")
        void recuperaTutti_dueCorsi() {
            Teacher insegnante = dati.creaInsegnantePredefinito();
            dati.creaCorsoPredefinito(insegnante);
            dati.creaCorsoOnline(insegnante);

            List<CourseResponse> corsi = courseService.getAllCourses();

            assertEquals(2, corsi.size());
        }

        @Test
        @DisplayName("Recupera tutti i corsi - lista vuota se non ce ne sono")
        void recuperaTutti_listaVuota() {
            List<CourseResponse> corsi = courseService.getAllCourses();

            assertTrue(corsi.isEmpty(), "La lista deve essere vuota se non ci sono corsi");
        }

        @Test
        @DisplayName("Recupera solo corsi online - filtra correttamente")
        void recuperaOnline_filtraCorrettamente() {
            Teacher insegnante = dati.creaInsegnantePredefinito();
            dati.creaCorsoPredefinito(insegnante);   // non online
            dati.creaCorsoOnline(insegnante);          // online

            List<CourseResponse> corsiOnline = courseService.getOnlineCourses();

            assertEquals(1, corsiOnline.size());
            assertTrue(corsiOnline.get(0).online());
            assertEquals("Chitarra Online", corsiOnline.get(0).nome());
        }

        @Test
        @DisplayName("Recupera corsi online - lista vuota se nessuno e' online")
        void recuperaOnline_listaVuota() {
            Teacher insegnante = dati.creaInsegnantePredefinito();
            dati.creaCorsoPredefinito(insegnante); // non online

            List<CourseResponse> corsiOnline = courseService.getOnlineCourses();

            assertTrue(corsiOnline.isEmpty(),
                    "La lista deve essere vuota se nessun corso e' online");
        }

        @Test
        @DisplayName("Verifica che la response contenga il nomeInsegnante quando assegnato")
        void trovaPerCodice_verificaNomeInsegnante() {
            Teacher insegnante = dati.creaInsegnantePredefinito();
            dati.creaCorsoPredefinito(insegnante);

            CourseResponse response = courseService.getCourseByCode("C001");

            assertEquals("Luigi Verdi", response.nomeInsegnante(),
                    "Il nomeInsegnante deve corrispondere all'insegnante assegnato");
        }

        @Test
        @DisplayName("Verifica che nomeInsegnante sia null quando il corso non ha insegnante")
        void trovaPerCodice_senzaInsegnante() {
            CourseRequest request = new CourseRequest(
                    "C001", "Corso Senza Insegnante",
                    LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30),
                    25.0, 40, false, Livello.PRINCIPIANTE);
            courseService.createCourse(request);

            CourseResponse response = courseService.getCourseByCode("C001");

            assertNull(response.nomeInsegnante(),
                    "Senza insegnante assegnato il nome deve essere null");
        }
    }

    // ================================================================
    // AGGIORNAMENTO CORSO
    // ================================================================

    @Nested
    @DisplayName("Aggiornamento Corso")
    class AggiornamentoCorso {

        @Test
        @DisplayName("Aggiorna corso - nome, costo e livello cambiati")
        void aggiornaCorso_successo() {
            Teacher insegnante = dati.creaInsegnantePredefinito();
            dati.creaCorsoPredefinito(insegnante);

            CourseRequest updateRequest = new CourseRequest(
                    "C001", "Pianoforte Avanzato",
                    LocalDate.of(2026, 3, 1), LocalDate.of(2026, 9, 30),
                    30.0, 60, false, Livello.AVANZATO);

            CourseResponse response = courseService.updateCourse("C001", updateRequest);

            assertAll("Verifica campi aggiornati",
                    () -> assertEquals("Pianoforte Avanzato", response.nome()),
                    () -> assertEquals(30.0 * 60, response.costoTotale()),
                    () -> assertEquals(Livello.AVANZATO, response.livello())
            );
        }

        @Test
        @DisplayName("Aggiorna corso con date invalide - deve lanciare BusinessRuleException")
        void aggiornaCorso_dateInvalide_errore() {
            Teacher insegnante = dati.creaInsegnantePredefinito();
            dati.creaCorsoPredefinito(insegnante);

            CourseRequest updateRequest = new CourseRequest(
                    "C001", "Test",
                    LocalDate.of(2026, 6, 30), LocalDate.of(2026, 3, 1),
                    25.0, 40, false, null);

            assertThrows(BusinessRuleException.class,
                    () -> courseService.updateCourse("C001", updateRequest),
                    "Date invalide nell'aggiornamento devono lanciare BusinessRuleException");
        }

        @Test
        @DisplayName("Aggiorna corso inesistente - deve lanciare ResourceNotFoundException")
        void aggiornaCorso_nonEsistente() {
            CourseRequest request = new CourseRequest(
                    "C999", "Test",
                    LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30),
                    25.0, 40, false, null);

            assertThrows(ResourceNotFoundException.class,
                    () -> courseService.updateCourse("C999", request));
        }

        @Test
        @DisplayName("Dopo l'aggiornamento, il codice corso resta immutato")
        void aggiornaCorso_codiceImmutato() {
            Teacher insegnante = dati.creaInsegnantePredefinito();
            dati.creaCorsoPredefinito(insegnante);

            CourseRequest updateRequest = new CourseRequest(
                    "C001", "Nuovo Nome",
                    LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30),
                    30.0, 50, true, Livello.AVANZATO);

            CourseResponse response = courseService.updateCourse("C001", updateRequest);

            assertEquals("C001", response.codiceCorso(),
                    "Il codice corso non deve cambiare dopo l'aggiornamento");
        }
    }

    // ================================================================
    // ELIMINAZIONE CORSO
    // ================================================================

    @Nested
    @DisplayName("Eliminazione Corso")
    class EliminazioneCorso {

        @Test
        @DisplayName("Elimina corso esistente - non deve lanciare eccezioni")
        void eliminaCorso_successo() {
            Teacher insegnante = dati.creaInsegnantePredefinito();
            dati.creaCorsoPredefinito(insegnante);

            assertDoesNotThrow(() -> courseService.deleteCourse("C001"));
            assertThrows(ResourceNotFoundException.class,
                    () -> courseService.getCourseByCode("C001"));
        }

        @Test
        @DisplayName("Elimina corso inesistente - deve lanciare ResourceNotFoundException")
        void eliminaCorso_nonEsistente() {
            assertThrows(ResourceNotFoundException.class,
                    () -> courseService.deleteCourse("C999"));
        }
    }

    // ================================================================
    // GESTIONE LEZIONI
    // ================================================================

    @Nested
    @DisplayName("Gestione Lezioni")
    class GestioneLezioni {

        private Teacher insegnante;

        @BeforeEach
        void preparaDati() {
            insegnante = dati.creaInsegnantePredefinito();
            dati.creaCorsoPredefinito(insegnante);
        }

        @Test
        @DisplayName("Aggiungi lezione a corso - verifica tutti i campi della response")
        void aggiungiLezione_successo() {
            LessonRequest request = new LessonRequest(
                    1, LocalDate.of(2026, 3, 5), LocalTime.of(10, 0),
                    60, "Aula 1", "Introduzione al pianoforte");

            LessonResponse response = courseService.addLesson("C001", request);

            assertAll("Verifica campi lezione",
                    () -> assertNotNull(response.id()),
                    () -> assertEquals(1, response.numero()),
                    () -> assertEquals(LocalDate.of(2026, 3, 5), response.data()),
                    () -> assertEquals(LocalTime.of(10, 0), response.oraInizio()),
                    () -> assertEquals(60, response.durata()),
                    () -> assertEquals("Aula 1", response.aula()),
                    () -> assertEquals("Introduzione al pianoforte", response.argomento())
            );
        }

        @Test
        @DisplayName("Aggiungi lezione con numero duplicato - deve lanciare DuplicateResourceException")
        void aggiungiLezione_numeroDuplicato_errore() {
            LessonRequest request1 = new LessonRequest(
                    1, LocalDate.of(2026, 3, 5), LocalTime.of(10, 0),
                    60, "Aula 1", "Lezione 1");
            courseService.addLesson("C001", request1);

            LessonRequest request2 = new LessonRequest(
                    1, LocalDate.of(2026, 3, 12), LocalTime.of(10, 0),
                    60, "Aula 2", "Lezione duplicata");

            assertThrows(DuplicateResourceException.class,
                    () -> courseService.addLesson("C001", request2),
                    "Numero lezione duplicato deve lanciare DuplicateResourceException");
        }

        @Test
        @DisplayName("Aggiungi lezione a corso inesistente - deve lanciare ResourceNotFoundException")
        void aggiungiLezione_corsoNonTrovato() {
            LessonRequest request = new LessonRequest(
                    1, LocalDate.of(2026, 3, 5), LocalTime.of(10, 0),
                    60, "Aula 1", "Test");

            assertThrows(ResourceNotFoundException.class,
                    () -> courseService.addLesson("INESISTENTE", request));
        }

        @Test
        @DisplayName("Aggiungi piu' lezioni allo stesso corso - verifica conteggio")
        void aggiungiPiuLezioni_successo() {
            LessonRequest lezione1 = new LessonRequest(
                    1, LocalDate.of(2026, 3, 5), LocalTime.of(10, 0),
                    60, "Aula 1", "Introduzione");
            LessonRequest lezione2 = new LessonRequest(
                    2, LocalDate.of(2026, 3, 12), LocalTime.of(10, 0),
                    60, "Aula 1", "Scale musicali");
            LessonRequest lezione3 = new LessonRequest(
                    3, LocalDate.of(2026, 3, 19), LocalTime.of(10, 0),
                    60, "Aula 1", "Accordi");

            courseService.addLesson("C001", lezione1);
            courseService.addLesson("C001", lezione2);
            courseService.addLesson("C001", lezione3);

            CourseResponse corso = courseService.getCourseByCode("C001");
            assertEquals(3, corso.lezioni().size(),
                    "Il corso deve avere 3 lezioni");
        }
    }

    // ================================================================
    // GESTIONE STRUMENTI DEL CORSO
    // ================================================================

    @Nested
    @DisplayName("Gestione Strumenti del Corso")
    class GestioneStrumentiCorso {

        private Teacher insegnante;

        @BeforeEach
        void preparaDati() {
            insegnante = dati.creaInsegnantePredefinito();
            dati.creaCorsoPredefinito(insegnante);
        }

        @Test
        @DisplayName("Aggiungi strumento a corso - successo senza eccezioni")
        void aggiungiStrumento_successo() {
            dati.creaStrumentoPredefinito();

            assertDoesNotThrow(() -> courseService.addInstrumentToCourse("C001", "S001"));
        }

        @Test
        @DisplayName("Aggiungi strumento gia' associato al corso - deve lanciare DuplicateResourceException")
        void aggiungiStrumento_duplicato_errore() {
            dati.creaStrumentoPredefinito();

            courseService.addInstrumentToCourse("C001", "S001");

            assertThrows(DuplicateResourceException.class,
                    () -> courseService.addInstrumentToCourse("C001", "S001"),
                    "Strumento gia' associato deve lanciare DuplicateResourceException");
        }

        @Test
        @DisplayName("Aggiungi strumento a corso inesistente - deve lanciare ResourceNotFoundException")
        void aggiungiStrumento_corsoNonTrovato() {
            dati.creaStrumentoPredefinito();

            assertThrows(ResourceNotFoundException.class,
                    () -> courseService.addInstrumentToCourse("INESISTENTE", "S001"));
        }

        @Test
        @DisplayName("Aggiungi strumento inesistente a corso - deve lanciare ResourceNotFoundException")
        void aggiungiStrumento_strumentoNonTrovato() {
            assertThrows(ResourceNotFoundException.class,
                    () -> courseService.addInstrumentToCourse("C001", "INESISTENTE"));
        }
    }
}
