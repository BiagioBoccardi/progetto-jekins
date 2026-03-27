package com.scuoladimusica.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.scuoladimusica.TestDataFactory;
import com.scuoladimusica.model.dto.request.InstrumentRequest;
import com.scuoladimusica.model.dto.request.LoanRequest;
import com.scuoladimusica.model.dto.request.ReturnRequest;
import com.scuoladimusica.model.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class InstrumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestDataFactory dati;

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Nested
    @DisplayName("POST /api/instruments")
    class CreateInstrumentEndpoint {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Admin crea strumento - 201")
        void createInstrument_success() throws Exception {
            InstrumentRequest request = new InstrumentRequest(
                    "S001", "Pianoforte a Coda", TipoStrumento.TASTIERA,
                    "Yamaha", 2020, null, null, null, null, null);

            mockMvc.perform(post("/api/instruments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.codiceStrumento").value("S001"))
                    .andExpect(jsonPath("$.disponibile").value(true));
        }

        @Test
        @WithMockUser(roles = "TEACHER")
        @DisplayName("Teacher non può creare strumenti - 403")
        void createInstrument_teacher_forbidden() throws Exception {
            InstrumentRequest request = new InstrumentRequest(
                    "S001", "Test", TipoStrumento.FIATO,
                    null, null, null, null, null, null, null);

            mockMvc.perform(post("/api/instruments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Utente non autenticato - 401")
        void createInstrument_unauthenticated() throws Exception {
            InstrumentRequest request = new InstrumentRequest(
                    "S001", "Test", TipoStrumento.FIATO,
                    null, null, null, null, null, null, null);

            mockMvc.perform(post("/api/instruments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/instruments")
    class GetInstrumentsEndpoint {

        @Test
        @WithMockUser(roles = "STUDENT")
        @DisplayName("Qualsiasi utente vede gli strumenti - 200")
        void getAllInstruments() throws Exception {
            dati.creaStrumentoPredefinito();
            dati.creaSecondoStrumento();

            mockMvc.perform(get("/api/instruments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));
        }

        @Test
        @WithMockUser(roles = "STUDENT")
        @DisplayName("Strumenti disponibili - 200")
        void getAvailableInstruments() throws Exception {
            Instrument instrument = dati.creaStrumentoPredefinito();
            dati.creaSecondoStrumento();
            Student student = dati.creaStudentePredefinito();
            dati.creaPrestito(instrument, student, LocalDate.of(2026, 3, 1));

            mockMvc.perform(get("/api/instruments/available"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].codiceStrumento").value("S002"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Strumento per codice - 200")
        void getInstrument_found() throws Exception {
            dati.creaStrumentoPredefinito();

            mockMvc.perform(get("/api/instruments/S001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nome").value("Pianoforte a Coda"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Strumento non trovato - 404")
        void getInstrument_notFound() throws Exception {
            mockMvc.perform(get("/api/instruments/INESISTENTE"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/instruments/{code}/loan")
    class LoanEndpoint {

        @Test
        @WithMockUser(roles = "TEACHER")
        @DisplayName("Teacher presta strumento - 201")
        void loanInstrument_success() throws Exception {
            dati.creaStrumentoPredefinito();
            dati.creaStudentePredefinito();

            LoanRequest request = new LoanRequest("M001", LocalDate.of(2026, 3, 1));

            mockMvc.perform(post("/api/instruments/S001/loan")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.codiceStrumento").value("S001"))
                    .andExpect(jsonPath("$.matricolaStudente").value("M001"))
                    .andExpect(jsonPath("$.dataFine").doesNotExist());
        }

        @Test
        @WithMockUser(roles = "TEACHER")
        @DisplayName("Presta strumento già in prestito - 400")
        void loanInstrument_alreadyLoaned() throws Exception {
            Instrument instrument = dati.creaStrumentoPredefinito();
            Student student = dati.creaStudentePredefinito();
            dati.creaPrestito(instrument, student, LocalDate.of(2026, 3, 1));
            dati.creaSecondoStudente();

            LoanRequest request = new LoanRequest("M002", LocalDate.of(2026, 3, 5));

            mockMvc.perform(post("/api/instruments/S001/loan")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "STUDENT")
        @DisplayName("Student non può prestare - 403")
        void loanInstrument_student_forbidden() throws Exception {
            LoanRequest request = new LoanRequest("M001", LocalDate.of(2026, 3, 1));

            mockMvc.perform(post("/api/instruments/S001/loan")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /api/instruments/{code}/return")
    class ReturnEndpoint {

        @Test
        @WithMockUser(roles = "TEACHER")
        @DisplayName("Teacher restituisce strumento - 200")
        void returnInstrument_success() throws Exception {
            Instrument instrument = dati.creaStrumentoPredefinito();
            Student student = dati.creaStudentePredefinito();
            dati.creaPrestito(instrument, student, LocalDate.of(2026, 3, 1));

            ReturnRequest request = new ReturnRequest(LocalDate.of(2026, 3, 15));

            mockMvc.perform(post("/api/instruments/S001/return")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dataFine").value("2026-03-15"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Restituisci strumento non in prestito - 400")
        void returnInstrument_notLoaned() throws Exception {
            dati.creaStrumentoPredefinito();

            ReturnRequest request = new ReturnRequest(LocalDate.of(2026, 3, 15));

            mockMvc.perform(post("/api/instruments/S001/return")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "STUDENT")
        @DisplayName("Student non può restituire - 403")
        void returnInstrument_student_forbidden() throws Exception {
            ReturnRequest request = new ReturnRequest(LocalDate.of(2026, 3, 15));

            mockMvc.perform(post("/api/instruments/S001/return")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }
}
