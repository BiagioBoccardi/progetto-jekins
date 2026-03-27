package com.scuoladimusica.service;

import com.scuoladimusica.TestDataFactory;
import com.scuoladimusica.exception.DuplicateResourceException;
import com.scuoladimusica.exception.ResourceNotFoundException;
import com.scuoladimusica.model.dto.request.StudentRequest;
import com.scuoladimusica.model.dto.response.StudentReportResponse;
import com.scuoladimusica.model.dto.response.StudentResponse;
import com.scuoladimusica.model.entity.*;
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
class StudentServiceTest {

    @Autowired
    private StudentService studentService;

    @Autowired
    private TestDataFactory dati;

    // ================================================================
    // CREAZIONE STUDENTE
    // ================================================================

    @Nested
    @DisplayName("Creazione Studente")
    class CreazioneStudente {

        @Test
        @DisplayName("Crea studente con tutti i campi compilati - verifica tutti i campi della response")
        void creaStudente_tuttiICampi_successo() {
            StudentRequest request = new StudentRequest(
                    "M001", "RSSMRA90A01H501Z", "Mario", "Rossi",
                    LocalDate.of(1990, 1, 1), "3331234567", Livello.PRINCIPIANTE);

            StudentResponse response = studentService.createStudent(request);

            assertAll("Verifica tutti i campi della response",
                    () -> assertNotNull(response.id(), "L'ID deve essere generato automaticamente"),
                    () -> assertEquals("M001", response.matricola()),
                    () -> assertEquals("RSSMRA90A01H501Z", response.cf()),
                    () -> assertEquals("Mario", response.nome()),
                    () -> assertEquals("Rossi", response.cognome()),
                    () -> assertEquals("Mario Rossi", response.nomeCompleto()),
                    () -> assertEquals(LocalDate.of(1990, 1, 1), response.dataNascita()),
                    () -> assertEquals("3331234567", response.telefono()),
                    () -> assertEquals(Livello.PRINCIPIANTE, response.livello()),
                    () -> assertEquals(0, response.numeroCorsiFrequentati()),
                    () -> assertEquals(0.0, response.mediaVoti())
            );
        }

        @Test
        @DisplayName("Crea studente senza livello - deve usare PRINCIPIANTE come default")
        void creaStudente_senzaLivello_defaultPrincipiante() {
            StudentRequest request = new StudentRequest(
                    "M001", "RSSMRA90A01H501Z", "Mario", "Rossi",
                    LocalDate.of(1990, 1, 1), null, null);

            StudentResponse response = studentService.createStudent(request);

            assertEquals(Livello.PRINCIPIANTE, response.livello(),
                    "Senza livello specificato deve essere PRINCIPIANTE");
        }

        @Test
        @DisplayName("Crea studente con livello INTERMEDIO")
        void creaStudente_livelloIntermedio() {
            StudentRequest request = new StudentRequest(
                    "M001", "RSSMRA90A01H501Z", "Mario", "Rossi",
                    LocalDate.of(1990, 1, 1), null, Livello.INTERMEDIO);

            assertEquals(Livello.INTERMEDIO, studentService.createStudent(request).livello());
        }

        @Test
        @DisplayName("Crea studente con livello AVANZATO")
        void creaStudente_livelloAvanzato() {
            StudentRequest request = new StudentRequest(
                    "M001", "RSSMRA90A01H501Z", "Mario", "Rossi",
                    LocalDate.of(1990, 1, 1), null, Livello.AVANZATO);

            assertEquals(Livello.AVANZATO, studentService.createStudent(request).livello());
        }

        @Test
        @DisplayName("Crea studente senza telefono - il telefono deve essere null")
        void creaStudente_senzaTelefono() {
            StudentRequest request = new StudentRequest(
                    "M001", "RSSMRA90A01H501Z", "Mario", "Rossi",
                    LocalDate.of(1990, 1, 1), null, null);

            assertNull(studentService.createStudent(request).telefono());
        }

        @Test
        @DisplayName("Crea studente con matricola già esistente - deve lanciare DuplicateResourceException")
        void creaStudente_matricolaDuplicata_errore() {
            dati.creaStudentePredefinito();

            StudentRequest request = new StudentRequest(
                    "M001", "BNCMRA85B02H501Z", "Maria", "Bianchi",
                    LocalDate.of(1985, 2, 2), null, null);

            assertThrows(DuplicateResourceException.class,
                    () -> studentService.createStudent(request),
                    "Matricola duplicata deve lanciare DuplicateResourceException");
        }

        @Test
        @DisplayName("Crea studente con codice fiscale già esistente - deve lanciare DuplicateResourceException")
        void creaStudente_cfDuplicato_errore() {
            dati.creaStudentePredefinito();

            StudentRequest request = new StudentRequest(
                    "M099", "RSSMRA90A01H501Z", "Altro", "Nome",
                    LocalDate.of(1985, 2, 2), null, null);

            assertThrows(DuplicateResourceException.class,
                    () -> studentService.createStudent(request),
                    "CF duplicato deve lanciare DuplicateResourceException");
        }

        @Test
        @DisplayName("Lo studente appena creato non ha corsi frequentati")
        void creaStudente_zeroCorsi() {
            StudentRequest request = new StudentRequest(
                    "M001", "RSSMRA90A01H501Z", "Mario", "Rossi",
                    LocalDate.of(1990, 1, 1), null, null);

            StudentResponse response = studentService.createStudent(request);

            assertEquals(0, response.numeroCorsiFrequentati());
            assertEquals(0.0, response.mediaVoti());
        }
    }

    // ================================================================
    // RICERCA STUDENTI
    // ================================================================

    @Nested
    @DisplayName("Ricerca Studenti")
    class RicercaStudenti {

        @Test
        @DisplayName("Trova studente per matricola - verifica dati corretti")
        void trovaPerMatricola_trovato() {
            dati.creaStudentePredefinito();

            StudentResponse response = studentService.getStudentByMatricola("M001");

            assertEquals("M001", response.matricola());
            assertEquals("Mario Rossi", response.nomeCompleto());
        }

        @Test
        @DisplayName("Trova studente per matricola inesistente - deve lanciare ResourceNotFoundException")
        void trovaPerMatricola_nonTrovato() {
            assertThrows(ResourceNotFoundException.class,
                    () -> studentService.getStudentByMatricola("INESISTENTE"),
                    "Matricola inesistente deve lanciare ResourceNotFoundException");
        }

        @Test
        @DisplayName("Recupera tutti gli studenti - lista con 2 elementi")
        void recuperaTutti_dueStudenti() {
            dati.creaStudentePredefinito();
            dati.creaSecondoStudente();

            List<StudentResponse> studenti = studentService.getAllStudents();

            assertEquals(2, studenti.size());
        }

        @Test
        @DisplayName("Recupera tutti gli studenti - lista vuota se non ce ne sono")
        void recuperaTutti_listaVuota() {
            List<StudentResponse> studenti = studentService.getAllStudents();

            assertTrue(studenti.isEmpty(), "La lista deve essere vuota se non ci sono studenti");
        }

        @Test
        @DisplayName("Recupera tutti gli studenti - verifica che ogni response abbia il nome completo")
        void recuperaTutti_verificaNomeCompleto() {
            dati.creaStudentePredefinito();
            dati.creaSecondoStudente();

            List<StudentResponse> studenti = studentService.getAllStudents();

            studenti.forEach(s ->
                    assertNotNull(s.nomeCompleto(), "Ogni studente deve avere il nome completo"));
        }

        @Test
        @DisplayName("Filtra studenti per livello PRINCIPIANTE")
        void filtraPerLivello_principiante() {
            dati.creaStudentePredefinito();  // PRINCIPIANTE
            dati.creaSecondoStudente();       // INTERMEDIO

            List<StudentResponse> principianti = studentService.getStudentsByLivello(Livello.PRINCIPIANTE);

            assertEquals(1, principianti.size());
            assertEquals("M001", principianti.get(0).matricola());
        }

        @Test
        @DisplayName("Filtra studenti per livello INTERMEDIO")
        void filtraPerLivello_intermedio() {
            dati.creaStudentePredefinito();  // PRINCIPIANTE
            dati.creaSecondoStudente();       // INTERMEDIO

            List<StudentResponse> intermedi = studentService.getStudentsByLivello(Livello.INTERMEDIO);

            assertEquals(1, intermedi.size());
            assertEquals("M002", intermedi.get(0).matricola());
        }

        @Test
        @DisplayName("Filtra studenti per livello senza risultati - lista vuota")
        void filtraPerLivello_nessunoTrovato() {
            dati.creaStudentePredefinito(); // PRINCIPIANTE

            List<StudentResponse> avanzati = studentService.getStudentsByLivello(Livello.AVANZATO);

            assertTrue(avanzati.isEmpty());
        }

        @Test
        @DisplayName("Filtra studenti per livello - più studenti con stesso livello")
        void filtraPerLivello_piuStudentiStessoLivello() {
            dati.creaStudentePredefinito(); // PRINCIPIANTE
            dati.creaStudente("M003", "VRDGPP78C03H501Z", "Giuseppe", "Verdi",
                    LocalDate.of(1978, 3, 3), Livello.PRINCIPIANTE);

            List<StudentResponse> principianti = studentService.getStudentsByLivello(Livello.PRINCIPIANTE);

            assertEquals(2, principianti.size());
        }
    }

    // ================================================================
    // AGGIORNAMENTO STUDENTE
    // ================================================================

    @Nested
    @DisplayName("Aggiornamento Studente")
    class AggiornamentoStudente {

        @Test
        @DisplayName("Aggiorna nome e telefono dello studente")
        void aggiornaStudente_nomeTelefono() {
            dati.creaStudentePredefinito();

            StudentRequest request = new StudentRequest(
                    "M001", "RSSMRA90A01H501Z", "Marco", "Rossi",
                    LocalDate.of(1990, 1, 1), "3339876543", Livello.PRINCIPIANTE);

            StudentResponse response = studentService.updateStudent("M001", request);

            assertEquals("Marco", response.nome());
            assertEquals("3339876543", response.telefono());
        }

        @Test
        @DisplayName("Aggiorna livello dello studente da PRINCIPIANTE a INTERMEDIO")
        void aggiornaStudente_cambiaLivello() {
            dati.creaStudentePredefinito();

            StudentRequest request = new StudentRequest(
                    "M001", "RSSMRA90A01H501Z", "Mario", "Rossi",
                    LocalDate.of(1990, 1, 1), null, Livello.INTERMEDIO);

            StudentResponse response = studentService.updateStudent("M001", request);

            assertEquals(Livello.INTERMEDIO, response.livello());
        }

        @Test
        @DisplayName("Aggiorna studente inesistente - deve lanciare ResourceNotFoundException")
        void aggiornaStudente_nonEsistente() {
            StudentRequest request = new StudentRequest(
                    "M999", "RSSMRA90A01H501Z", "Test", "Test",
                    LocalDate.of(1990, 1, 1), null, null);

            assertThrows(ResourceNotFoundException.class,
                    () -> studentService.updateStudent("M999", request));
        }

        @Test
        @DisplayName("Dopo l'aggiornamento, la matricola resta immutata")
        void aggiornaStudente_matricolaImmutata() {
            dati.creaStudentePredefinito();

            StudentRequest request = new StudentRequest(
                    "M001", "RSSMRA90A01H501Z", "NuovoNome", "NuovoCognome",
                    LocalDate.of(1990, 1, 1), null, null);

            StudentResponse response = studentService.updateStudent("M001", request);

            assertEquals("M001", response.matricola(), "La matricola non deve cambiare");
        }
    }

    // ================================================================
    // ELIMINAZIONE STUDENTE
    // ================================================================

    @Nested
    @DisplayName("Eliminazione Studente")
    class EliminazioneStudente {

        @Test
        @DisplayName("Elimina studente esistente - non deve lanciare eccezioni")
        void eliminaStudente_successo() {
            dati.creaStudentePredefinito();

            assertDoesNotThrow(() -> studentService.deleteStudent("M001"));
        }

        @Test
        @DisplayName("Dopo l'eliminazione, lo studente non è più trovabile")
        void eliminaStudente_nonPiuTrovabile() {
            dati.creaStudentePredefinito();

            studentService.deleteStudent("M001");

            assertThrows(ResourceNotFoundException.class,
                    () -> studentService.getStudentByMatricola("M001"));
        }

        @Test
        @DisplayName("Elimina studente inesistente - deve lanciare ResourceNotFoundException")
        void eliminaStudente_nonEsistente() {
            assertThrows(ResourceNotFoundException.class,
                    () -> studentService.deleteStudent("M999"));
        }

        @Test
        @DisplayName("Dopo l'eliminazione, la lista studenti si riduce di 1")
        void eliminaStudente_listaRidotta() {
            dati.creaStudentePredefinito();
            dati.creaSecondoStudente();

            studentService.deleteStudent("M001");

            assertEquals(1, studentService.getAllStudents().size());
        }
    }

    // ================================================================
    // REPORT STUDENTE
    // ================================================================

    @Nested
    @DisplayName("Report Studente")
    class ReportStudente {

        @Test
        @DisplayName("Report studente senza corsi - tutti i conteggi a zero")
        void report_senzaCorsi() {
            dati.creaStudentePredefinito();

            StudentReportResponse report = studentService.getStudentReport("M001");

            assertAll("Report studente senza corsi",
                    () -> assertEquals("Mario Rossi", report.studente()),
                    () -> assertEquals(Livello.PRINCIPIANTE, report.livello()),
                    () -> assertEquals(0, report.numCorsi()),
                    () -> assertEquals(0.0, report.mediaVoti()),
                    () -> assertTrue(report.corsi().isEmpty())
            );
        }

        @Test
        @DisplayName("Report studente con un corso e un voto")
        void report_unCorsoConVoto() {
            Student studente = dati.creaStudentePredefinito();
            Teacher insegnante = dati.creaInsegnantePredefinito();
            Course corso = dati.creaCorsoPredefinito(insegnante);
            dati.creaIscrizioneConVoto(studente, corso, 2026, 28);

            StudentReportResponse report = studentService.getStudentReport("M001");

            assertEquals(1, report.numCorsi());
            assertEquals(28.0, report.mediaVoti(), 0.01);
            assertEquals(1, report.corsi().size());
            assertTrue(report.corsi().contains("Pianoforte Base"));
        }

        @Test
        @DisplayName("Report studente con più corsi e media voti")
        void report_piuCorsiConMedia() {
            Student studente = dati.creaStudentePredefinito();
            Teacher insegnante = dati.creaInsegnantePredefinito();
            Course corso1 = dati.creaCorsoPredefinito(insegnante);
            Course corso2 = dati.creaCorsoOnline(insegnante);

            dati.creaIscrizioneConVoto(studente, corso1, 2026, 24);
            dati.creaIscrizioneConVoto(studente, corso2, 2026, 30);

            StudentReportResponse report = studentService.getStudentReport("M001");

            assertEquals(2, report.numCorsi());
            assertEquals(27.0, report.mediaVoti(), 0.01, "Media di 24 e 30 deve essere 27");
            assertEquals(2, report.corsi().size());
        }

        @Test
        @DisplayName("Report studente con corsi ma senza voti - media deve essere 0")
        void report_corsiSenzaVoti() {
            Student studente = dati.creaStudentePredefinito();
            Teacher insegnante = dati.creaInsegnantePredefinito();
            Course corso = dati.creaCorsoPredefinito(insegnante);
            dati.creaIscrizione(studente, corso, 2026);

            StudentReportResponse report = studentService.getStudentReport("M001");

            assertEquals(1, report.numCorsi());
            assertEquals(0.0, report.mediaVoti(), 0.01,
                    "Senza voti registrati la media deve essere 0");
        }

        @Test
        @DisplayName("Report studente con mix di corsi con e senza voto")
        void report_mixCorsiConESenzaVoto() {
            Student studente = dati.creaStudentePredefinito();
            Teacher insegnante = dati.creaInsegnantePredefinito();
            Course corso1 = dati.creaCorsoPredefinito(insegnante);
            Course corso2 = dati.creaCorsoOnline(insegnante);

            dati.creaIscrizioneConVoto(studente, corso1, 2026, 26);
            dati.creaIscrizione(studente, corso2, 2026); // senza voto

            StudentReportResponse report = studentService.getStudentReport("M001");

            assertEquals(2, report.numCorsi());
            assertEquals(26.0, report.mediaVoti(), 0.01,
                    "La media deve considerare solo i corsi con voto");
        }

        @Test
        @DisplayName("Report studente inesistente - deve lanciare ResourceNotFoundException")
        void report_studenteNonEsistente() {
            assertThrows(ResourceNotFoundException.class,
                    () -> studentService.getStudentReport("M999"));
        }

        @Test
        @DisplayName("Report contiene i nomi corretti dei corsi")
        void report_nomiCorsiCorretti() {
            Student studente = dati.creaStudentePredefinito();
            Teacher insegnante = dati.creaInsegnantePredefinito();
            Course corso1 = dati.creaCorsoPredefinito(insegnante);
            Course corso2 = dati.creaCorsoOnline(insegnante);

            dati.creaIscrizione(studente, corso1, 2026);
            dati.creaIscrizione(studente, corso2, 2026);

            StudentReportResponse report = studentService.getStudentReport("M001");

            assertTrue(report.corsi().contains("Pianoforte Base"));
            assertTrue(report.corsi().contains("Chitarra Online"));
        }
    }
}
