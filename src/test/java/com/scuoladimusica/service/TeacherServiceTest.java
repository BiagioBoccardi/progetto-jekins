package com.scuoladimusica.service;

import com.scuoladimusica.TestDataFactory;
import com.scuoladimusica.exception.BusinessRuleException;
import com.scuoladimusica.exception.DuplicateResourceException;
import com.scuoladimusica.exception.ResourceNotFoundException;
import com.scuoladimusica.model.dto.request.TeacherRequest;
import com.scuoladimusica.model.dto.response.CourseResponse;
import com.scuoladimusica.model.dto.response.TeacherResponse;
import com.scuoladimusica.model.entity.Livello;
import com.scuoladimusica.model.entity.Teacher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TeacherServiceTest {

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private TestDataFactory dati;

    // ================================================================
    // CREAZIONE INSEGNANTE
    // ================================================================

    @Nested
    @DisplayName("Creazione Insegnante")
    class CreazioneInsegnante {

        @Test
        @DisplayName("Crea insegnante con tutti i campi - verifica tutti i campi della response")
        void creaInsegnante_tuttiICampi_successo() {
            TeacherRequest request = new TeacherRequest(
                    "I001", "VRDLGU80A01H501Z", "Luigi", "Verdi",
                    LocalDate.of(1980, 1, 1), "3331234567", 2500.0, "Pianoforte", 15);

            TeacherResponse response = teacherService.createTeacher(request);

            assertAll("Verifica tutti i campi della response",
                    () -> assertNotNull(response.id(), "L'ID deve essere generato automaticamente"),
                    () -> assertEquals("I001", response.matricolaInsegnante()),
                    () -> assertEquals("VRDLGU80A01H501Z", response.cf()),
                    () -> assertEquals("Luigi", response.nome()),
                    () -> assertEquals("Verdi", response.cognome()),
                    () -> assertEquals("Luigi Verdi", response.nomeCompleto()),
                    () -> assertEquals(LocalDate.of(1980, 1, 1), response.dataNascita()),
                    () -> assertEquals("3331234567", response.telefono()),
                    () -> assertEquals(2500.0, response.stipendio()),
                    () -> assertEquals("Pianoforte", response.specializzazione()),
                    () -> assertEquals(15, response.anniEsperienza()),
                    () -> assertEquals(0, response.numeroCorsiTenuti())
            );
        }

        @Test
        @DisplayName("Crea insegnante con esperienza 0 - il valore di default deve essere 0")
        void creaInsegnante_esperienzaZero() {
            TeacherRequest request = new TeacherRequest(
                    "I001", "VRDLGU80A01H501Z", "Luigi", "Verdi",
                    LocalDate.of(1980, 1, 1), null, 2500.0, "Pianoforte", 0);

            TeacherResponse response = teacherService.createTeacher(request);

            assertEquals(0, response.anniEsperienza(),
                    "L'esperienza deve essere 0 quando specificato");
        }

        @Test
        @DisplayName("Crea insegnante senza telefono - il telefono deve essere null")
        void creaInsegnante_senzaTelefono() {
            TeacherRequest request = new TeacherRequest(
                    "I001", "VRDLGU80A01H501Z", "Luigi", "Verdi",
                    LocalDate.of(1980, 1, 1), null, 2500.0, "Pianoforte", 15);

            TeacherResponse response = teacherService.createTeacher(request);

            assertNull(response.telefono(), "Il telefono deve essere null se non fornito");
        }

        @Test
        @DisplayName("Crea insegnante con matricola duplicata - deve lanciare DuplicateResourceException")
        void creaInsegnante_matricolaDuplicata_errore() {
            dati.creaInsegnantePredefinito();

            TeacherRequest request = new TeacherRequest(
                    "I001", "ALTROCF12345678AB", "Altro", "Nome",
                    LocalDate.of(1985, 5, 5), null, 2000.0, "Violino", 5);

            assertThrows(DuplicateResourceException.class,
                    () -> teacherService.createTeacher(request),
                    "Matricola duplicata deve lanciare DuplicateResourceException");
        }

        @Test
        @DisplayName("Crea insegnante con CF duplicato - deve lanciare DuplicateResourceException")
        void creaInsegnante_cfDuplicato_errore() {
            dati.creaInsegnantePredefinito();

            TeacherRequest request = new TeacherRequest(
                    "I099", "VRDLGU80A01H501Z", "Altro", "Nome",
                    LocalDate.of(1985, 5, 5), null, 2000.0, "Violino", 5);

            assertThrows(DuplicateResourceException.class,
                    () -> teacherService.createTeacher(request),
                    "CF duplicato deve lanciare DuplicateResourceException");
        }

        @Test
        @DisplayName("Verifica che il nomeCompleto sia la concatenazione di nome e cognome")
        void creaInsegnante_verificaNomeCompleto() {
            TeacherRequest request = new TeacherRequest(
                    "I001", "VRDLGU80A01H501Z", "Luigi", "Verdi",
                    LocalDate.of(1980, 1, 1), null, 2500.0, "Pianoforte", 15);

            TeacherResponse response = teacherService.createTeacher(request);

            assertEquals("Luigi Verdi", response.nomeCompleto(),
                    "Il nomeCompleto deve essere 'nome cognome'");
        }

        @Test
        @DisplayName("L'insegnante appena creato non ha corsi assegnati")
        void creaInsegnante_zeroCorsi() {
            TeacherRequest request = new TeacherRequest(
                    "I001", "VRDLGU80A01H501Z", "Luigi", "Verdi",
                    LocalDate.of(1980, 1, 1), null, 2500.0, "Pianoforte", 15);

            TeacherResponse response = teacherService.createTeacher(request);

            assertEquals(0, response.numeroCorsiTenuti(),
                    "Un insegnante appena creato non deve avere corsi");
        }
    }

    // ================================================================
    // RICERCA INSEGNANTI
    // ================================================================

    @Nested
    @DisplayName("Ricerca Insegnanti")
    class RicercaInsegnanti {

        @Test
        @DisplayName("Trova insegnante per matricola - verifica dati corretti")
        void trovaPerMatricola_trovato() {
            dati.creaInsegnantePredefinito();

            TeacherResponse response = teacherService.getTeacherByMatricola("I001");

            assertEquals("I001", response.matricolaInsegnante());
            assertEquals("Luigi Verdi", response.nomeCompleto());
        }

        @Test
        @DisplayName("Trova insegnante per matricola inesistente - deve lanciare ResourceNotFoundException")
        void trovaPerMatricola_nonTrovato() {
            assertThrows(ResourceNotFoundException.class,
                    () -> teacherService.getTeacherByMatricola("INESISTENTE"),
                    "Matricola inesistente deve lanciare ResourceNotFoundException");
        }

        @Test
        @DisplayName("Recupera tutti gli insegnanti - lista con 2 elementi")
        void recuperaTutti_dueInsegnanti() {
            dati.creaInsegnantePredefinito();
            dati.creaSecondoInsegnante();

            List<TeacherResponse> insegnanti = teacherService.getAllTeachers();

            assertEquals(2, insegnanti.size());
        }

        @Test
        @DisplayName("Recupera tutti gli insegnanti - lista vuota se non ce ne sono")
        void recuperaTutti_listaVuota() {
            List<TeacherResponse> insegnanti = teacherService.getAllTeachers();

            assertTrue(insegnanti.isEmpty(), "La lista deve essere vuota se non ci sono insegnanti");
        }

        @Test
        @DisplayName("Verifica tutti i campi della response dopo la ricerca per matricola")
        void trovaPerMatricola_verificaCampiResponse() {
            dati.creaInsegnantePredefinito();

            TeacherResponse response = teacherService.getTeacherByMatricola("I001");

            assertAll("Verifica campi response",
                    () -> assertNotNull(response.id()),
                    () -> assertEquals("I001", response.matricolaInsegnante()),
                    () -> assertEquals("VRDLGU80A01H501Z", response.cf()),
                    () -> assertEquals("Luigi", response.nome()),
                    () -> assertEquals("Verdi", response.cognome()),
                    () -> assertEquals(2500.0, response.stipendio()),
                    () -> assertEquals("Pianoforte", response.specializzazione()),
                    () -> assertEquals(15, response.anniEsperienza())
            );
        }
    }

    // ================================================================
    // AGGIORNAMENTO INSEGNANTE
    // ================================================================

    @Nested
    @DisplayName("Aggiornamento Insegnante")
    class AggiornamentoInsegnante {

        @Test
        @DisplayName("Aggiorna nome dell'insegnante")
        void aggiornaInsegnante_nome() {
            dati.creaInsegnantePredefinito();

            TeacherRequest request = new TeacherRequest(
                    "I001", "VRDLGU80A01H501Z", "Marco", "Verdi",
                    LocalDate.of(1980, 1, 1), null, 2500.0, "Pianoforte", 15);

            TeacherResponse response = teacherService.updateTeacher("I001", request);

            assertEquals("Marco", response.nome());
            assertEquals("Marco Verdi", response.nomeCompleto());
        }

        @Test
        @DisplayName("Aggiorna stipendio dell'insegnante")
        void aggiornaInsegnante_stipendio() {
            dati.creaInsegnantePredefinito();

            TeacherRequest request = new TeacherRequest(
                    "I001", "VRDLGU80A01H501Z", "Luigi", "Verdi",
                    LocalDate.of(1980, 1, 1), null, 3000.0, "Pianoforte", 15);

            TeacherResponse response = teacherService.updateTeacher("I001", request);

            assertEquals(3000.0, response.stipendio());
        }

        @Test
        @DisplayName("Aggiorna specializzazione dell'insegnante")
        void aggiornaInsegnante_specializzazione() {
            dati.creaInsegnantePredefinito();

            TeacherRequest request = new TeacherRequest(
                    "I001", "VRDLGU80A01H501Z", "Luigi", "Verdi",
                    LocalDate.of(1980, 1, 1), null, 2500.0, "Pianoforte e Composizione", 15);

            TeacherResponse response = teacherService.updateTeacher("I001", request);

            assertEquals("Pianoforte e Composizione", response.specializzazione());
        }

        @Test
        @DisplayName("Aggiorna anni di esperienza dell'insegnante")
        void aggiornaInsegnante_esperienza() {
            dati.creaInsegnantePredefinito();

            TeacherRequest request = new TeacherRequest(
                    "I001", "VRDLGU80A01H501Z", "Luigi", "Verdi",
                    LocalDate.of(1980, 1, 1), null, 2500.0, "Pianoforte", 20);

            TeacherResponse response = teacherService.updateTeacher("I001", request);

            assertEquals(20, response.anniEsperienza());
        }

        @Test
        @DisplayName("Aggiorna insegnante inesistente - deve lanciare ResourceNotFoundException")
        void aggiornaInsegnante_nonEsistente() {
            TeacherRequest request = new TeacherRequest(
                    "I999", "VRDLGU80A01H501Z", "Test", "Test",
                    LocalDate.of(1980, 1, 1), null, 2000.0, "Test", 0);

            assertThrows(ResourceNotFoundException.class,
                    () -> teacherService.updateTeacher("I999", request));
        }

        @Test
        @DisplayName("Dopo l'aggiornamento, la matricola resta immutata")
        void aggiornaInsegnante_matricolaImmutata() {
            dati.creaInsegnantePredefinito();

            TeacherRequest request = new TeacherRequest(
                    "I001", "VRDLGU80A01H501Z", "NuovoNome", "NuovoCognome",
                    LocalDate.of(1980, 1, 1), "3339876543", 3000.0, "Violino", 20);

            TeacherResponse response = teacherService.updateTeacher("I001", request);

            assertEquals("I001", response.matricolaInsegnante(),
                    "La matricola non deve cambiare dopo l'aggiornamento");
        }
    }

    // ================================================================
    // ELIMINAZIONE INSEGNANTE
    // ================================================================

    @Nested
    @DisplayName("Eliminazione Insegnante")
    class EliminazioneInsegnante {

        @Test
        @DisplayName("Elimina insegnante esistente - non deve lanciare eccezioni")
        void eliminaInsegnante_successo() {
            dati.creaInsegnantePredefinito();

            assertDoesNotThrow(() -> teacherService.deleteTeacher("I001"));
        }

        @Test
        @DisplayName("Dopo l'eliminazione, l'insegnante non e' piu' trovabile")
        void eliminaInsegnante_nonPiuTrovabile() {
            dati.creaInsegnantePredefinito();

            teacherService.deleteTeacher("I001");

            assertThrows(ResourceNotFoundException.class,
                    () -> teacherService.getTeacherByMatricola("I001"));
        }

        @Test
        @DisplayName("Elimina insegnante inesistente - deve lanciare ResourceNotFoundException")
        void eliminaInsegnante_nonEsistente() {
            assertThrows(ResourceNotFoundException.class,
                    () -> teacherService.deleteTeacher("I999"));
        }

        @Test
        @DisplayName("Dopo l'eliminazione, la lista insegnanti si riduce di 1")
        void eliminaInsegnante_listaRidotta() {
            dati.creaInsegnantePredefinito();
            dati.creaSecondoInsegnante();

            teacherService.deleteTeacher("I001");

            assertEquals(1, teacherService.getAllTeachers().size());
        }
    }

    // ================================================================
    // ASSEGNAZIONE CORSO
    // ================================================================

    @Nested
    @DisplayName("Assegnazione Corso")
    class AssegnazioneCorso {

        @Test
        @DisplayName("Assegna corso a insegnante - successo senza eccezioni")
        void assegnaCorso_successo() {
            dati.creaInsegnantePredefinito();
            dati.creaCorso("C001", "Pianoforte Base",
                    LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30),
                    25.0, 40, false, Livello.PRINCIPIANTE, null);

            assertDoesNotThrow(() -> teacherService.assignCourse("I001", "C001"));
        }

        @Test
        @DisplayName("Assegna corso gia' assegnato a un altro insegnante - deve lanciare BusinessRuleException")
        void assegnaCorso_giaAssegnato_errore() {
            Teacher insegnante = dati.creaInsegnantePredefinito();
            dati.creaCorsoPredefinito(insegnante); // C001 gia' assegnato

            assertThrows(BusinessRuleException.class,
                    () -> teacherService.assignCourse("I001", "C001"));
        }

        @Test
        @DisplayName("Assegna corso - insegnante non trovato")
        void assegnaCorso_insegnanteNonTrovato() {
            assertThrows(ResourceNotFoundException.class,
                    () -> teacherService.assignCourse("INESISTENTE", "C001"));
        }

        @Test
        @DisplayName("Assegna corso - corso non trovato")
        void assegnaCorso_corsoNonTrovato() {
            dati.creaInsegnantePredefinito();

            assertThrows(ResourceNotFoundException.class,
                    () -> teacherService.assignCourse("I001", "INESISTENTE"));
        }

        @Test
        @DisplayName("Dopo l'assegnazione, il corso mostra il nome dell'insegnante")
        void assegnaCorso_verificaInsegnanteNelCorso() {
            dati.creaInsegnantePredefinito();
            dati.creaCorso("C001", "Pianoforte Base",
                    LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30),
                    25.0, 40, false, Livello.PRINCIPIANTE, null);

            teacherService.assignCourse("I001", "C001");

            CourseResponse corso = courseService.getCourseByCode("C001");
            assertEquals("Luigi Verdi", corso.nomeInsegnante(),
                    "Dopo l'assegnazione, il corso deve mostrare il nome dell'insegnante");
        }

        @Test
        @DisplayName("Dopo l'assegnazione, il conteggio corsi tenuti aumenta")
        void assegnaCorso_conteggioCorsiAumenta() {
            dati.creaInsegnantePredefinito();
            dati.creaCorso("C001", "Pianoforte Base",
                    LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30),
                    25.0, 40, false, Livello.PRINCIPIANTE, null);

            teacherService.assignCourse("I001", "C001");

            TeacherResponse insegnante = teacherService.getTeacherByMatricola("I001");
            assertEquals(1, insegnante.numeroCorsiTenuti(),
                    "Dopo l'assegnazione di un corso, il conteggio deve essere 1");
        }
    }
}
