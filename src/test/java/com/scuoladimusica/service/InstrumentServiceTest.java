package com.scuoladimusica.service;

import com.scuoladimusica.TestDataFactory;
import com.scuoladimusica.exception.BusinessRuleException;
import com.scuoladimusica.exception.DuplicateResourceException;
import com.scuoladimusica.exception.ResourceNotFoundException;
import com.scuoladimusica.model.dto.request.InstrumentRequest;
import com.scuoladimusica.model.dto.response.InstrumentResponse;
import com.scuoladimusica.model.dto.response.LoanResponse;
import com.scuoladimusica.model.entity.*;
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
class InstrumentServiceTest {

    @Autowired
    private InstrumentService instrumentService;

    @Autowired
    private TestDataFactory dati;

    // ================================================================
    // CREAZIONE STRUMENTO
    // ================================================================

    @Nested
    @DisplayName("Creazione Strumento")
    class CreazioneStrumento {

        @Test
        @DisplayName("Crea strumento a tastiera - verifica tutti i campi della response")
        void creaStrumento_tastiera_successo() {
            InstrumentRequest request = new InstrumentRequest(
                    "S001", "Pianoforte a Coda", TipoStrumento.TASTIERA,
                    "Yamaha", 2020, null, null, null, null, null);

            InstrumentResponse response = instrumentService.createInstrument(request);

            assertAll("Verifica tutti i campi della response",
                    () -> assertNotNull(response.id(), "L'ID deve essere generato automaticamente"),
                    () -> assertEquals("S001", response.codiceStrumento()),
                    () -> assertEquals("Pianoforte a Coda", response.nome()),
                    () -> assertEquals(TipoStrumento.TASTIERA, response.tipoStrumento()),
                    () -> assertEquals("Yamaha", response.marca()),
                    () -> assertEquals(2020, response.annoProduzione()),
                    () -> assertTrue(response.disponibile(), "Uno strumento appena creato deve essere disponibile")
            );
        }

        @Test
        @DisplayName("Crea strumento a corda - verifica tipo corretto")
        void creaStrumento_corda_successo() {
            InstrumentRequest request = new InstrumentRequest(
                    "S001", "Chitarra Classica", TipoStrumento.CORDA,
                    "Fender", 2019, 6, "Nylon", "Mogano", null, null);

            InstrumentResponse response = instrumentService.createInstrument(request);

            assertEquals(TipoStrumento.CORDA, response.tipoStrumento());
            assertEquals("Chitarra Classica", response.nome());
            assertTrue(response.disponibile());
        }

        @Test
        @DisplayName("Crea strumento a percussione - verifica tipo corretto")
        void creaStrumento_percussione_successo() {
            InstrumentRequest request = new InstrumentRequest(
                    "S001", "Batteria Acustica", TipoStrumento.PERCUSSIONE,
                    "Pearl", 2021, null, null, null, "Naturale", 14.0);

            InstrumentResponse response = instrumentService.createInstrument(request);

            assertEquals(TipoStrumento.PERCUSSIONE, response.tipoStrumento());
            assertEquals("Batteria Acustica", response.nome());
            assertTrue(response.disponibile());
        }

        @Test
        @DisplayName("Crea strumento con codice duplicato - deve lanciare DuplicateResourceException")
        void creaStrumento_codiceDuplicato_errore() {
            dati.creaStrumentoPredefinito();

            InstrumentRequest request = new InstrumentRequest(
                    "S001", "Altro Strumento", TipoStrumento.FIATO,
                    null, null, null, null, null, null, null);

            assertThrows(DuplicateResourceException.class,
                    () -> instrumentService.createInstrument(request),
                    "Codice duplicato deve lanciare DuplicateResourceException");
        }

        @Test
        @DisplayName("Verifica che uno strumento appena creato sia disponibile")
        void creaStrumento_disponibileInizialmente() {
            InstrumentRequest request = new InstrumentRequest(
                    "S001", "Pianoforte", TipoStrumento.TASTIERA,
                    "Yamaha", 2020, null, null, null, null, null);

            InstrumentResponse response = instrumentService.createInstrument(request);

            assertTrue(response.disponibile(),
                    "Lo strumento deve essere disponibile alla creazione");
        }
    }

    // ================================================================
    // RICERCA STRUMENTI
    // ================================================================

    @Nested
    @DisplayName("Ricerca Strumenti")
    class RicercaStrumenti {

        @Test
        @DisplayName("Trova strumento per codice - verifica dati corretti")
        void trovaPerCodice_trovato() {
            dati.creaStrumentoPredefinito();

            InstrumentResponse response = instrumentService.getInstrumentByCode("S001");

            assertEquals("S001", response.codiceStrumento());
            assertEquals("Pianoforte a Coda", response.nome());
            assertTrue(response.disponibile());
        }

        @Test
        @DisplayName("Trova strumento per codice inesistente - deve lanciare ResourceNotFoundException")
        void trovaPerCodice_nonTrovato() {
            assertThrows(ResourceNotFoundException.class,
                    () -> instrumentService.getInstrumentByCode("INESISTENTE"),
                    "Codice inesistente deve lanciare ResourceNotFoundException");
        }

        @Test
        @DisplayName("Recupera tutti gli strumenti - lista con 2 elementi")
        void recuperaTutti_dueStrumenti() {
            dati.creaStrumentoPredefinito();
            dati.creaSecondoStrumento();

            List<InstrumentResponse> strumenti = instrumentService.getAllInstruments();

            assertEquals(2, strumenti.size());
        }

        @Test
        @DisplayName("Recupera tutti gli strumenti - lista vuota se non ce ne sono")
        void recuperaTutti_listaVuota() {
            List<InstrumentResponse> strumenti = instrumentService.getAllInstruments();

            assertTrue(strumenti.isEmpty(), "La lista deve essere vuota se non ci sono strumenti");
        }

        @Test
        @DisplayName("Recupera strumenti disponibili - tutti disponibili se nessuno e' in prestito")
        void recuperaDisponibili_tuttiDisponibili() {
            dati.creaStrumentoPredefinito();
            dati.creaSecondoStrumento();

            List<InstrumentResponse> disponibili = instrumentService.getAvailableInstruments();

            assertEquals(2, disponibili.size());
        }

        @Test
        @DisplayName("Recupera strumenti disponibili - uno in prestito, ne resta uno")
        void recuperaDisponibili_unoInPrestito() {
            Instrument strumento = dati.creaStrumentoPredefinito();
            dati.creaSecondoStrumento();
            Student studente = dati.creaStudentePredefinito();
            dati.creaPrestito(strumento, studente, LocalDate.of(2026, 3, 1));

            List<InstrumentResponse> disponibili = instrumentService.getAvailableInstruments();

            assertEquals(1, disponibili.size());
            assertEquals("S002", disponibili.get(0).codiceStrumento());
        }

        @Test
        @DisplayName("Recupera strumenti disponibili - tutti in prestito, lista vuota")
        void recuperaDisponibili_tuttiInPrestito() {
            Instrument strumento1 = dati.creaStrumentoPredefinito();
            Instrument strumento2 = dati.creaSecondoStrumento();
            Student studente1 = dati.creaStudentePredefinito();
            Student studente2 = dati.creaSecondoStudente();
            dati.creaPrestito(strumento1, studente1, LocalDate.of(2026, 3, 1));
            dati.creaPrestito(strumento2, studente2, LocalDate.of(2026, 3, 1));

            List<InstrumentResponse> disponibili = instrumentService.getAvailableInstruments();

            assertTrue(disponibili.isEmpty(),
                    "Nessuno strumento deve essere disponibile se tutti sono in prestito");
        }

        @Test
        @DisplayName("Verifica flag disponibile - false quando lo strumento e' in prestito")
        void verificaFlagDisponibile_inPrestito() {
            Instrument strumento = dati.creaStrumentoPredefinito();
            Student studente = dati.creaStudentePredefinito();
            dati.creaPrestito(strumento, studente, LocalDate.of(2026, 3, 1));

            InstrumentResponse response = instrumentService.getInstrumentByCode("S001");

            assertFalse(response.disponibile(),
                    "Lo strumento in prestito non deve risultare disponibile");
        }
    }

    // ================================================================
    // PRESTITO STRUMENTO
    // ================================================================

    @Nested
    @DisplayName("Prestito Strumento")
    class PrestitoStrumento {

        @Test
        @DisplayName("Presta strumento a studente - verifica tutti i campi della response")
        void prestaStrumento_successo() {
            dati.creaStrumentoPredefinito();
            dati.creaStudentePredefinito();

            LoanResponse response = instrumentService.loanToStudent(
                    "S001", "M001", LocalDate.of(2026, 3, 1));

            assertAll("Verifica tutti i campi della response del prestito",
                    () -> assertNotNull(response.id()),
                    () -> assertEquals("S001", response.codiceStrumento()),
                    () -> assertEquals("Pianoforte a Coda", response.nomeStrumento()),
                    () -> assertEquals("M001", response.matricolaStudente()),
                    () -> assertEquals("Mario Rossi", response.nomeStudente()),
                    () -> assertEquals(LocalDate.of(2026, 3, 1), response.dataInizio()),
                    () -> assertNull(response.dataFine(), "Il prestito attivo non deve avere data fine")
            );
        }

        @Test
        @DisplayName("Presta strumento gia' in prestito - deve lanciare BusinessRuleException")
        void prestaStrumento_giaInPrestito_errore() {
            Instrument strumento = dati.creaStrumentoPredefinito();
            Student studente = dati.creaStudentePredefinito();
            dati.creaPrestito(strumento, studente, LocalDate.of(2026, 3, 1));

            dati.creaSecondoStudente();

            assertThrows(BusinessRuleException.class,
                    () -> instrumentService.loanToStudent(
                            "S001", "M002", LocalDate.of(2026, 3, 5)),
                    "Strumento gia' in prestito deve lanciare BusinessRuleException");
        }

        @Test
        @DisplayName("Presta strumento non trovato - deve lanciare ResourceNotFoundException")
        void prestaStrumento_strumentoNonTrovato() {
            dati.creaStudentePredefinito();

            assertThrows(ResourceNotFoundException.class,
                    () -> instrumentService.loanToStudent(
                            "INESISTENTE", "M001", LocalDate.of(2026, 3, 1)));
        }

        @Test
        @DisplayName("Presta strumento a studente non trovato - deve lanciare ResourceNotFoundException")
        void prestaStrumento_studenteNonTrovato() {
            dati.creaStrumentoPredefinito();

            assertThrows(ResourceNotFoundException.class,
                    () -> instrumentService.loanToStudent(
                            "S001", "INESISTENTE", LocalDate.of(2026, 3, 1)));
        }
    }

    // ================================================================
    // RESTITUZIONE STRUMENTO
    // ================================================================

    @Nested
    @DisplayName("Restituzione Strumento")
    class RestituzioneStrumento {

        @Test
        @DisplayName("Restituisci strumento - verifica response con date corrette")
        void restituisciStrumento_successo() {
            Instrument strumento = dati.creaStrumentoPredefinito();
            Student studente = dati.creaStudentePredefinito();
            dati.creaPrestito(strumento, studente, LocalDate.of(2026, 3, 1));

            LoanResponse response = instrumentService.returnInstrument(
                    "S001", LocalDate.of(2026, 3, 15));

            assertAll("Verifica response della restituzione",
                    () -> assertEquals(LocalDate.of(2026, 3, 1), response.dataInizio()),
                    () -> assertEquals(LocalDate.of(2026, 3, 15), response.dataFine()),
                    () -> assertEquals("S001", response.codiceStrumento()),
                    () -> assertEquals("Pianoforte a Coda", response.nomeStrumento()),
                    () -> assertEquals("M001", response.matricolaStudente()),
                    () -> assertEquals("Mario Rossi", response.nomeStudente())
            );
        }

        @Test
        @DisplayName("Restituisci strumento non in prestito - deve lanciare BusinessRuleException")
        void restituisciStrumento_nonInPrestito_errore() {
            dati.creaStrumentoPredefinito();

            assertThrows(BusinessRuleException.class,
                    () -> instrumentService.returnInstrument(
                            "S001", LocalDate.of(2026, 3, 15)),
                    "Strumento non in prestito deve lanciare BusinessRuleException");
        }

        @Test
        @DisplayName("Restituisci con data precedente all'inizio del prestito - deve lanciare BusinessRuleException")
        void restituisciStrumento_dataPrimaInizio_errore() {
            Instrument strumento = dati.creaStrumentoPredefinito();
            Student studente = dati.creaStudentePredefinito();
            dati.creaPrestito(strumento, studente, LocalDate.of(2026, 3, 10));

            assertThrows(BusinessRuleException.class,
                    () -> instrumentService.returnInstrument(
                            "S001", LocalDate.of(2026, 3, 1)),
                    "Data restituzione prima della data inizio deve lanciare BusinessRuleException");
        }

        @Test
        @DisplayName("Restituisci strumento non trovato - deve lanciare ResourceNotFoundException")
        void restituisciStrumento_nonTrovato() {
            assertThrows(ResourceNotFoundException.class,
                    () -> instrumentService.returnInstrument(
                            "INESISTENTE", LocalDate.of(2026, 3, 15)));
        }

        @Test
        @DisplayName("Verifica che la response della restituzione contenga entrambe le date")
        void restituisciStrumento_verificaResponseConDate() {
            Instrument strumento = dati.creaStrumentoPredefinito();
            Student studente = dati.creaStudentePredefinito();
            dati.creaPrestito(strumento, studente, LocalDate.of(2026, 3, 1));

            LoanResponse response = instrumentService.returnInstrument(
                    "S001", LocalDate.of(2026, 3, 20));

            assertNotNull(response.dataInizio(), "La data inizio deve essere presente");
            assertNotNull(response.dataFine(), "La data fine deve essere presente dopo la restituzione");
        }
    }

    // ================================================================
    // PRESTITO SUCCESSIVO E DISPONIBILITA' DOPO RESTITUZIONE
    // ================================================================

    @Nested
    @DisplayName("Prestito successivo e disponibilita' dopo restituzione")
    class PrestitoSuccessivo {

        @Test
        @DisplayName("Prestito successivo dopo restituzione - successo con nuovo studente")
        void prestitoDopoRestituzione_successo() {
            Instrument strumento = dati.creaStrumentoPredefinito();
            Student studente = dati.creaStudentePredefinito();
            dati.creaPrestito(strumento, studente, LocalDate.of(2026, 3, 1));

            instrumentService.returnInstrument("S001", LocalDate.of(2026, 3, 10));

            dati.creaSecondoStudente();
            LoanResponse response = instrumentService.loanToStudent(
                    "S001", "M002", LocalDate.of(2026, 3, 15));

            assertEquals("M002", response.matricolaStudente());
            assertNull(response.dataFine(), "Il nuovo prestito non deve avere data fine");
        }

        @Test
        @DisplayName("Strumento risulta disponibile dopo la restituzione")
        void strumentoDisponibileDopoRestituzione() {
            Instrument strumento = dati.creaStrumentoPredefinito();
            Student studente = dati.creaStudentePredefinito();
            dati.creaPrestito(strumento, studente, LocalDate.of(2026, 3, 1));

            // Verifica che non e' disponibile durante il prestito
            assertFalse(instrumentService.getInstrumentByCode("S001").disponibile());

            instrumentService.returnInstrument("S001", LocalDate.of(2026, 3, 10));

            // Verifica che e' disponibile dopo la restituzione
            assertTrue(instrumentService.getInstrumentByCode("S001").disponibile(),
                    "Lo strumento deve risultare disponibile dopo la restituzione");
        }

        @Test
        @DisplayName("Strumento compare nella lista dei disponibili dopo la restituzione")
        void strumentoInListaDisponibiliDopoRestituzione() {
            Instrument strumento = dati.creaStrumentoPredefinito();
            Student studente = dati.creaStudentePredefinito();
            dati.creaPrestito(strumento, studente, LocalDate.of(2026, 3, 1));

            // Durante il prestito non deve comparire tra i disponibili
            assertTrue(instrumentService.getAvailableInstruments().isEmpty());

            instrumentService.returnInstrument("S001", LocalDate.of(2026, 3, 10));

            // Dopo la restituzione deve comparire tra i disponibili
            List<InstrumentResponse> disponibili = instrumentService.getAvailableInstruments();
            assertEquals(1, disponibili.size());
            assertEquals("S001", disponibili.get(0).codiceStrumento());
        }
    }
}
