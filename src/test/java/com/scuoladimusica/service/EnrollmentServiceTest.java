package com.scuoladimusica.service;

import com.scuoladimusica.TestDataFactory;
import com.scuoladimusica.exception.BusinessRuleException;
import com.scuoladimusica.exception.DuplicateResourceException;
import com.scuoladimusica.exception.ResourceNotFoundException;
import com.scuoladimusica.model.dto.response.EnrollmentResponse;
import com.scuoladimusica.model.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class EnrollmentServiceTest {

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private TestDataFactory dati;

    // ================================================================
    // ISCRIZIONE STUDENTE
    // ================================================================

    @Nested
    @DisplayName("Iscrizione Studente a Corso")
    class IscrizioneStudente {

        private Student studente;
        private Teacher insegnante;
        private Course corso;

        @BeforeEach
        void preparaDati() {
            studente = dati.creaStudentePredefinito();
            insegnante = dati.creaInsegnantePredefinito();
            corso = dati.creaCorsoPredefinito(insegnante);
        }

        @Test
        @DisplayName("Iscrivi studente a corso - verifica tutti i campi della response")
        void iscriviStudente_successo() {
            EnrollmentResponse response = enrollmentService.enrollStudent("M001", "C001", 2026);

            assertAll("Verifica tutti i campi della response",
                    () -> assertNotNull(response.id(), "L'ID deve essere generato automaticamente"),
                    () -> assertEquals("M001", response.matricolaStudente()),
                    () -> assertEquals("Mario Rossi", response.nomeStudente()),
                    () -> assertEquals("C001", response.codiceCorso()),
                    () -> assertEquals("Pianoforte Base", response.nomeCorso()),
                    () -> assertEquals(2026, response.annoIscrizione()),
                    () -> assertNull(response.votoFinale(), "Il voto iniziale deve essere null")
            );
        }

        @Test
        @DisplayName("Iscrivi studente gia' iscritto allo stesso corso - deve lanciare DuplicateResourceException")
        void iscriviStudente_duplicata_errore() {
            enrollmentService.enrollStudent("M001", "C001", 2026);

            assertThrows(DuplicateResourceException.class,
                    () -> enrollmentService.enrollStudent("M001", "C001", 2026),
                    "Iscrizione duplicata deve lanciare DuplicateResourceException");
        }

        @Test
        @DisplayName("Iscrivi lo stesso studente a piu' corsi - successo")
        void iscriviStudente_piuCorsi_successo() {
            Course corso2 = dati.creaCorsoOnline(insegnante);

            enrollmentService.enrollStudent("M001", "C001", 2026);
            EnrollmentResponse response2 = enrollmentService.enrollStudent("M001", "C002", 2026);

            assertEquals("Chitarra Online", response2.nomeCorso());
            assertEquals("M001", response2.matricolaStudente());
        }

        @Test
        @DisplayName("Iscrivi piu' studenti allo stesso corso - successo")
        void iscriviPiuStudenti_stessoCorso() {
            Student studente2 = dati.creaSecondoStudente();

            EnrollmentResponse resp1 = enrollmentService.enrollStudent("M001", "C001", 2026);
            EnrollmentResponse resp2 = enrollmentService.enrollStudent("M002", "C001", 2026);

            assertAll("Verifica iscrizioni di studenti diversi",
                    () -> assertEquals("Mario Rossi", resp1.nomeStudente()),
                    () -> assertEquals("Maria Bianchi", resp2.nomeStudente()),
                    () -> assertEquals("C001", resp1.codiceCorso()),
                    () -> assertEquals("C001", resp2.codiceCorso())
            );
        }

        @Test
        @DisplayName("Iscrivi studente inesistente - deve lanciare ResourceNotFoundException")
        void iscriviStudente_studenteNonTrovato() {
            assertThrows(ResourceNotFoundException.class,
                    () -> enrollmentService.enrollStudent("INESISTENTE", "C001", 2026),
                    "Studente inesistente deve lanciare ResourceNotFoundException");
        }

        @Test
        @DisplayName("Iscrivi studente a corso inesistente - deve lanciare ResourceNotFoundException")
        void iscriviStudente_corsoNonTrovato() {
            assertThrows(ResourceNotFoundException.class,
                    () -> enrollmentService.enrollStudent("M001", "INESISTENTE", 2026),
                    "Corso inesistente deve lanciare ResourceNotFoundException");
        }

        @Test
        @DisplayName("Verifica che la response contenga tutti i dati dello studente e del corso")
        void iscriviStudente_verificaResponseCompleta() {
            EnrollmentResponse response = enrollmentService.enrollStudent("M001", "C001", 2026);

            assertNotNull(response.id());
            assertNotNull(response.matricolaStudente());
            assertNotNull(response.nomeStudente());
            assertNotNull(response.codiceCorso());
            assertNotNull(response.nomeCorso());
            assertTrue(response.annoIscrizione() > 0);
        }
    }

    // ================================================================
    // REGISTRAZIONE VOTO
    // ================================================================

    @Nested
    @DisplayName("Registrazione Voto")
    class RegistrazioneVoto {

        @BeforeEach
        void preparaDati() {
            dati.creaStudentePredefinito();
            Teacher insegnante = dati.creaInsegnantePredefinito();
            dati.creaCorsoPredefinito(insegnante);
            enrollmentService.enrollStudent("M001", "C001", 2026);
        }

        @Test
        @DisplayName("Registra voto valido 28 - successo")
        void registraVoto_successo() {
            EnrollmentResponse response = enrollmentService.registerVote("M001", "C001", 28);

            assertEquals(28, response.votoFinale());
        }

        @Test
        @DisplayName("Registra voto minimo 18 - successo")
        void registraVoto_minimo18_successo() {
            EnrollmentResponse response = enrollmentService.registerVote("M001", "C001", 18);

            assertEquals(18, response.votoFinale(),
                    "Il voto minimo 18 deve essere accettato");
        }

        @Test
        @DisplayName("Registra voto massimo 30 - successo")
        void registraVoto_massimo30_successo() {
            EnrollmentResponse response = enrollmentService.registerVote("M001", "C001", 30);

            assertEquals(30, response.votoFinale(),
                    "Il voto massimo 30 deve essere accettato");
        }

        @Test
        @DisplayName("Registra voto 17 (troppo basso) - deve lanciare BusinessRuleException")
        void registraVoto_troppoBasso_errore() {
            assertThrows(BusinessRuleException.class,
                    () -> enrollmentService.registerVote("M001", "C001", 17),
                    "Voto 17 (sotto il minimo) deve lanciare BusinessRuleException");
        }

        @Test
        @DisplayName("Registra voto 31 (troppo alto) - deve lanciare BusinessRuleException")
        void registraVoto_troppoAlto_errore() {
            assertThrows(BusinessRuleException.class,
                    () -> enrollmentService.registerVote("M001", "C001", 31),
                    "Voto 31 (sopra il massimo) deve lanciare BusinessRuleException");
        }

        @Test
        @DisplayName("Verifica che il voto sia presente nella response dopo la registrazione")
        void registraVoto_verificaVotoNellaResponse() {
            EnrollmentResponse response = enrollmentService.registerVote("M001", "C001", 25);

            assertNotNull(response.votoFinale(), "Il voto deve essere presente nella response");
            assertEquals(25, response.votoFinale());
            assertEquals("M001", response.matricolaStudente());
            assertEquals("C001", response.codiceCorso());
        }
    }

    @Nested
    @DisplayName("Registrazione Voto - Iscrizione non trovata")
    class RegistrazioneVotoIscrizioneNonTrovata {

        @Test
        @DisplayName("Registra voto per iscrizione inesistente - deve lanciare ResourceNotFoundException")
        void registraVoto_iscrizioneNonTrovata() {
            dati.creaStudentePredefinito();
            Teacher insegnante = dati.creaInsegnantePredefinito();
            dati.creaCorsoPredefinito(insegnante);
            // Non iscriviamo lo studente al corso

            assertThrows(ResourceNotFoundException.class,
                    () -> enrollmentService.registerVote("M001", "C001", 28),
                    "Iscrizione non trovata deve lanciare ResourceNotFoundException");
        }
    }

    // ================================================================
    // RICERCA ISCRIZIONI
    // ================================================================

    @Nested
    @DisplayName("Ricerca Iscrizioni per Studente")
    class RicercaIscrizioniPerStudente {

        @Test
        @DisplayName("Iscrizioni per studente - con risultati su piu' corsi")
        void iscrizioniPerStudente_conRisultati() {
            dati.creaStudentePredefinito();
            Teacher insegnante = dati.creaInsegnantePredefinito();
            dati.creaCorsoPredefinito(insegnante);
            Course corso2 = dati.creaCorsoOnline(insegnante);

            enrollmentService.enrollStudent("M001", "C001", 2026);
            enrollmentService.enrollStudent("M001", "C002", 2026);

            List<EnrollmentResponse> iscrizioni =
                    enrollmentService.getEnrollmentsByStudent("M001");

            assertEquals(2, iscrizioni.size());
        }

        @Test
        @DisplayName("Iscrizioni per studente - lista vuota se non e' iscritto a nessun corso")
        void iscrizioniPerStudente_senzaRisultati() {
            dati.creaStudentePredefinito();

            List<EnrollmentResponse> iscrizioni =
                    enrollmentService.getEnrollmentsByStudent("M001");

            assertTrue(iscrizioni.isEmpty(),
                    "La lista deve essere vuota se lo studente non ha iscrizioni");
        }

        @Test
        @DisplayName("Iscrizioni per studente inesistente - deve lanciare ResourceNotFoundException")
        void iscrizioniPerStudente_studenteNonTrovato() {
            assertThrows(ResourceNotFoundException.class,
                    () -> enrollmentService.getEnrollmentsByStudent("INESISTENTE"),
                    "Studente inesistente deve lanciare ResourceNotFoundException");
        }
    }

    @Nested
    @DisplayName("Ricerca Iscrizioni per Corso")
    class RicercaIscrizioniPerCorso {

        @Test
        @DisplayName("Iscrizioni per corso - con piu' studenti iscritti")
        void iscrizioniPerCorso_conRisultati() {
            dati.creaStudentePredefinito();
            dati.creaSecondoStudente();
            Teacher insegnante = dati.creaInsegnantePredefinito();
            dati.creaCorsoPredefinito(insegnante);

            enrollmentService.enrollStudent("M001", "C001", 2026);
            enrollmentService.enrollStudent("M002", "C001", 2026);

            List<EnrollmentResponse> iscrizioni =
                    enrollmentService.getEnrollmentsByCourse("C001");

            assertEquals(2, iscrizioni.size());
        }

        @Test
        @DisplayName("Iscrizioni per corso inesistente - deve lanciare ResourceNotFoundException")
        void iscrizioniPerCorso_corsoNonTrovato() {
            assertThrows(ResourceNotFoundException.class,
                    () -> enrollmentService.getEnrollmentsByCourse("INESISTENTE"),
                    "Corso inesistente deve lanciare ResourceNotFoundException");
        }

        @Test
        @DisplayName("Iscrizioni per corso - lista vuota se nessuno e' iscritto")
        void iscrizioniPerCorso_listaVuota() {
            Teacher insegnante = dati.creaInsegnantePredefinito();
            dati.creaCorsoPredefinito(insegnante);

            List<EnrollmentResponse> iscrizioni =
                    enrollmentService.getEnrollmentsByCourse("C001");

            assertTrue(iscrizioni.isEmpty(),
                    "La lista deve essere vuota se nessuno e' iscritto al corso");
        }
    }
}
