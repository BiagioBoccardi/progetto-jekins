package com.scuoladimusica.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.scuoladimusica.TestDataFactory;
import com.scuoladimusica.model.dto.request.EnrollmentRequest;
import com.scuoladimusica.model.dto.request.VoteRequest;
import com.scuoladimusica.model.entity.*;
import org.junit.jupiter.api.BeforeEach;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EnrollmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestDataFactory dati;

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    // ========== ISCRIZIONE ==========

    @Nested
    @DisplayName("POST /api/enrollments - Iscrizione studente a corso")
    class IscrizioneStudente {

        private Student studente;
        private Course corso;

        @BeforeEach
        void preparaDati() {
            studente = dati.creaStudentePredefinito();
            Teacher insegnante = dati.creaInsegnantePredefinito();
            corso = dati.creaCorsoPredefinito(insegnante);
        }

        @Test
        @WithMockUser(roles = "STUDENT")
        @DisplayName("Lo studente si iscrive a un corso con successo e riceve 201 Created")
        void studenteSiIscrive_201() throws Exception {
            EnrollmentRequest request = new EnrollmentRequest("M001", "C001", 2026);

            mockMvc.perform(post("/api/enrollments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.matricolaStudente").value("M001"))
                    .andExpect(jsonPath("$.codiceCorso").value("C001"))
                    .andExpect(jsonPath("$.nomeCorso").value("Pianoforte Base"))
                    .andExpect(jsonPath("$.annoIscrizione").value(2026))
                    .andExpect(jsonPath("$.votoFinale").doesNotExist());
        }

        @Test
        @WithMockUser(roles = "STUDENT")
        @DisplayName("La risposta dell'iscrizione contiene tutti i campi attesi nel body")
        void verificaBodyIscrizione() throws Exception {
            EnrollmentRequest request = new EnrollmentRequest("M001", "C001", 2026);

            mockMvc.perform(post("/api/enrollments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.matricolaStudente").value("M001"))
                    .andExpect(jsonPath("$.nomeStudente").exists())
                    .andExpect(jsonPath("$.codiceCorso").value("C001"))
                    .andExpect(jsonPath("$.nomeCorso").value("Pianoforte Base"))
                    .andExpect(jsonPath("$.annoIscrizione").value(2026));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("L'admin iscrive uno studente a un corso con successo e riceve 201 Created")
        void adminIscrive_201() throws Exception {
            EnrollmentRequest request = new EnrollmentRequest("M001", "C001", 2026);

            mockMvc.perform(post("/api/enrollments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("L'iscrizione duplicata allo stesso corso restituisce 409 Conflict")
        void iscrizioneDuplicata_409() throws Exception {
            dati.creaIscrizione(studente, corso, 2026);

            EnrollmentRequest request = new EnrollmentRequest("M001", "C001", 2026);

            mockMvc.perform(post("/api/enrollments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Un utente non autenticato non puo' iscriversi e riceve 401 Unauthorized")
        void utenteNonAutenticato_401() throws Exception {
            EnrollmentRequest request = new EnrollmentRequest("M001", "C001", 2026);

            mockMvc.perform(post("/api/enrollments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("L'iscrizione con studente inesistente restituisce 404 Not Found")
        void studenteInesistente_404() throws Exception {
            EnrollmentRequest request = new EnrollmentRequest("INESISTENTE", "C001", 2026);

            mockMvc.perform(post("/api/enrollments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("L'iscrizione con corso inesistente restituisce 404 Not Found")
        void corsoInesistente_404() throws Exception {
            EnrollmentRequest request = new EnrollmentRequest("M001", "INESISTENTE", 2026);

            mockMvc.perform(post("/api/enrollments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    // ========== REGISTRAZIONE VOTO ==========

    @Nested
    @DisplayName("POST /api/enrollments/{matricola}/{codiceCorso}/vote - Registrazione voto")
    class RegistrazioneVoto {

        private Student studente;
        private Course corso;

        @BeforeEach
        void preparaDati() {
            studente = dati.creaStudentePredefinito();
            Teacher insegnante = dati.creaInsegnantePredefinito();
            corso = dati.creaCorsoPredefinito(insegnante);
        }

        @Test
        @WithMockUser(roles = "TEACHER")
        @DisplayName("L'insegnante registra un voto con successo e riceve 200 OK")
        void insegnanteRegistraVoto_200() throws Exception {
            dati.creaIscrizione(studente, corso, 2026);

            VoteRequest request = new VoteRequest(28);

            mockMvc.perform(post("/api/enrollments/M001/C001/vote")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.votoFinale").value(28));
        }

        @Test
        @WithMockUser(roles = "TEACHER")
        @DisplayName("L'insegnante registra il voto massimo 30 con successo e riceve 200 OK")
        void insegnanteRegistraVotoMassimo_200() throws Exception {
            dati.creaIscrizione(studente, corso, 2026);

            VoteRequest request = new VoteRequest(30);

            mockMvc.perform(post("/api/enrollments/M001/C001/vote")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.votoFinale").value(30));
        }

        @Test
        @WithMockUser(roles = "TEACHER")
        @DisplayName("La risposta del voto contiene il voto registrato nel body")
        void verificaVotoNelBody() throws Exception {
            dati.creaIscrizione(studente, corso, 2026);

            VoteRequest request = new VoteRequest(25);

            mockMvc.perform(post("/api/enrollments/M001/C001/vote")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.votoFinale").value(25))
                    .andExpect(jsonPath("$.matricolaStudente").value("M001"))
                    .andExpect(jsonPath("$.codiceCorso").value("C001"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("L'admin registra un voto con successo e riceve 200 OK")
        void adminRegistraVoto_200() throws Exception {
            dati.creaIscrizione(studente, corso, 2026);

            VoteRequest request = new VoteRequest(28);

            mockMvc.perform(post("/api/enrollments/M001/C001/vote")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.votoFinale").value(28));
        }

        @Test
        @WithMockUser(roles = "STUDENT")
        @DisplayName("Lo studente non puo' registrare voti e riceve 403 Forbidden")
        void studenteRegistraVoto_403() throws Exception {
            VoteRequest request = new VoteRequest(28);

            mockMvc.perform(post("/api/enrollments/M001/C001/vote")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "TEACHER")
        @DisplayName("La registrazione voto su iscrizione non trovata restituisce 404 Not Found")
        void iscrizioneNonTrovata_404() throws Exception {
            VoteRequest request = new VoteRequest(28);

            mockMvc.perform(post("/api/enrollments/M001/C001/vote")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    // ========== ISCRIZIONI PER STUDENTE ==========

    @Nested
    @DisplayName("GET /api/enrollments/student/{matricola} - Iscrizioni per studente")
    class IscrizioniPerStudente {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("L'admin recupera le iscrizioni di uno studente con risultati e riceve 200 OK")
        void adminRecuperaIscrizioni_200() throws Exception {
            Student studente = dati.creaStudentePredefinito();
            Teacher insegnante = dati.creaInsegnantePredefinito();
            Course corso = dati.creaCorsoPredefinito(insegnante);
            dati.creaIscrizione(studente, corso, 2026);

            mockMvc.perform(get("/api/enrollments/student/M001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].matricolaStudente").value("M001"))
                    .andExpect(jsonPath("$[0].codiceCorso").value("C001"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("L'admin recupera le iscrizioni con voto e i dati sono corretti")
        void adminRecuperaIscrizioniConVoto_200() throws Exception {
            Student studente = dati.creaStudentePredefinito();
            Teacher insegnante = dati.creaInsegnantePredefinito();
            Course corso = dati.creaCorsoPredefinito(insegnante);
            dati.creaIscrizioneConVoto(studente, corso, 2026, 28);

            mockMvc.perform(get("/api/enrollments/student/M001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].votoFinale").value(28));
        }

        @Test
        @WithMockUser(roles = "TEACHER")
        @DisplayName("L'insegnante recupera le iscrizioni di uno studente e riceve 200 OK")
        void insegnanteRecuperaIscrizioni_200() throws Exception {
            Student studente = dati.creaStudentePredefinito();
            Teacher insegnante = dati.creaInsegnantePredefinito();
            Course corso = dati.creaCorsoPredefinito(insegnante);
            dati.creaIscrizione(studente, corso, 2026);

            mockMvc.perform(get("/api/enrollments/student/M001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));
        }

        @Test
        @WithMockUser(roles = "STUDENT")
        @DisplayName("Lo studente non puo' vedere le iscrizioni altrui e riceve 403 Forbidden")
        void studenteRecuperaIscrizioni_403() throws Exception {
            mockMvc.perform(get("/api/enrollments/student/M001"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Le iscrizioni di uno studente inesistente restituiscono 404 Not Found")
        void studenteInesistente_404() throws Exception {
            mockMvc.perform(get("/api/enrollments/student/INESISTENTE"))
                    .andExpect(status().isNotFound());
        }
    }

    // ========== ISCRIZIONI PER CORSO ==========

    @Nested
    @DisplayName("GET /api/enrollments/course/{codiceCorso} - Iscrizioni per corso")
    class IscrizioniPerCorso {

        @Test
        @WithMockUser(roles = "TEACHER")
        @DisplayName("L'insegnante recupera le iscrizioni di un corso e riceve 200 OK")
        void insegnanteRecuperaIscrizioni_200() throws Exception {
            Student studente = dati.creaStudentePredefinito();
            Student studente2 = dati.creaSecondoStudente();
            Teacher insegnante = dati.creaInsegnantePredefinito();
            Course corso = dati.creaCorsoPredefinito(insegnante);
            dati.creaIscrizione(studente, corso, 2026);
            dati.creaIscrizione(studente2, corso, 2026);

            mockMvc.perform(get("/api/enrollments/course/C001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("L'admin recupera le iscrizioni di un corso e riceve 200 OK")
        void adminRecuperaIscrizioni_200() throws Exception {
            Student studente = dati.creaStudentePredefinito();
            Teacher insegnante = dati.creaInsegnantePredefinito();
            Course corso = dati.creaCorsoPredefinito(insegnante);
            dati.creaIscrizione(studente, corso, 2026);

            mockMvc.perform(get("/api/enrollments/course/C001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].codiceCorso").value("C001"));
        }

        @Test
        @WithMockUser(roles = "STUDENT")
        @DisplayName("Lo studente non puo' vedere le iscrizioni per corso e riceve 403 Forbidden")
        void studenteRecuperaIscrizioni_403() throws Exception {
            mockMvc.perform(get("/api/enrollments/course/C001"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Le iscrizioni di un corso inesistente restituiscono 404 Not Found")
        void corsoInesistente_404() throws Exception {
            mockMvc.perform(get("/api/enrollments/course/INESISTENTE"))
                    .andExpect(status().isNotFound());
        }
    }
}
