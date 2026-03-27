package com.scuoladimusica.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.scuoladimusica.TestDataFactory;
import com.scuoladimusica.model.dto.request.CourseRequest;
import com.scuoladimusica.model.dto.request.LessonRequest;
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

import java.time.LocalDate;
import java.time.LocalTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestDataFactory dati;

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    // ========== CREAZIONE ==========

    @Nested
    @DisplayName("POST /api/courses - Creazione corso")
    class CreazioneCorso {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("L'admin crea un corso con successo e riceve 201 Created")
        void adminCreaCorso_201() throws Exception {
            CourseRequest request = new CourseRequest(
                    "C001", "Pianoforte Base",
                    LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30),
                    25.0, 40, false, Livello.PRINCIPIANTE);

            mockMvc.perform(post("/api/courses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.codiceCorso").value("C001"))
                    .andExpect(jsonPath("$.costoTotale").value(1000.0));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("L'admin crea un corso e il body contiene tutti i campi attesi")
        void adminCreaCorso_verificaBody() throws Exception {
            CourseRequest request = new CourseRequest(
                    "C001", "Pianoforte Base",
                    LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30),
                    25.0, 40, false, Livello.PRINCIPIANTE);

            mockMvc.perform(post("/api/courses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.codiceCorso").value("C001"))
                    .andExpect(jsonPath("$.nome").value("Pianoforte Base"))
                    .andExpect(jsonPath("$.costoOrario").value(25.0))
                    .andExpect(jsonPath("$.totaleOre").value(40))
                    .andExpect(jsonPath("$.online").value(false))
                    .andExpect(jsonPath("$.livello").value("PRINCIPIANTE"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("L'admin crea un corso online con successo")
        void adminCreaCorsoOnline_201() throws Exception {
            CourseRequest request = new CourseRequest(
                    "C002", "Chitarra Online",
                    LocalDate.of(2026, 4, 1), LocalDate.of(2026, 7, 31),
                    20.0, 30, true, Livello.INTERMEDIO);

            mockMvc.perform(post("/api/courses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.online").value(true));
        }

        @Test
        @WithMockUser(roles = "TEACHER")
        @DisplayName("L'insegnante non puo' creare corsi e riceve 403 Forbidden")
        void insegnanteCreaCorso_403() throws Exception {
            CourseRequest request = new CourseRequest(
                    "C001", "Test", LocalDate.of(2026, 3, 1),
                    LocalDate.of(2026, 6, 30), 25.0, 40, false, null);

            mockMvc.perform(post("/api/courses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Un utente non autenticato non puo' creare corsi e riceve 401 Unauthorized")
        void utenteNonAutenticato_401() throws Exception {
            CourseRequest request = new CourseRequest(
                    "C001", "Test", LocalDate.of(2026, 3, 1),
                    LocalDate.of(2026, 6, 30), 25.0, 40, false, null);

            mockMvc.perform(post("/api/courses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("La creazione con date invalide (fine prima di inizio) restituisce 400 Bad Request")
        void dateInvalide_400() throws Exception {
            CourseRequest request = new CourseRequest(
                    "C001", "Test",
                    LocalDate.of(2026, 6, 30), LocalDate.of(2026, 3, 1),
                    25.0, 40, false, null);

            mockMvc.perform(post("/api/courses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("La creazione con codice duplicato restituisce 409 Conflict")
        void codiceDuplicato_409() throws Exception {
            Teacher insegnante = dati.creaInsegnantePredefinito();
            dati.creaCorsoPredefinito(insegnante);

            CourseRequest request = new CourseRequest(
                    "C001", "Altro Corso",
                    LocalDate.of(2026, 5, 1), LocalDate.of(2026, 9, 30),
                    30.0, 50, false, Livello.INTERMEDIO);

            mockMvc.perform(post("/api/courses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("La creazione con nome vuoto restituisce 400 Bad Request")
        void nomeVuoto_400() throws Exception {
            CourseRequest request = new CourseRequest(
                    "C001", "",
                    LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30),
                    25.0, 40, false, null);

            mockMvc.perform(post("/api/courses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ========== LETTURA TUTTI ==========

    @Nested
    @DisplayName("GET /api/courses - Recupero tutti i corsi")
    class RecuperoCorsi {

        private Teacher insegnante;

        @BeforeEach
        void preparaDati() {
            insegnante = dati.creaInsegnantePredefinito();
            dati.creaCorsoPredefinito(insegnante);
            dati.creaCorsoOnline(insegnante);
        }

        @Test
        @WithMockUser(roles = "STUDENT")
        @DisplayName("Qualsiasi utente autenticato puo' vedere i corsi e riceve 200 OK")
        void studenteRecuperaTutti_200() throws Exception {
            mockMvc.perform(get("/api/courses"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));
        }

        @Test
        @WithMockUser(roles = "STUDENT")
        @DisplayName("La lista dei corsi contiene le dimensioni corrette")
        void verificaDimensione() throws Exception {
            mockMvc.perform(get("/api/courses"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));
        }
    }

    // ========== CORSI ONLINE ==========

    @Nested
    @DisplayName("GET /api/courses/online - Recupero corsi online")
    class RecuperoCorsiOnline {

        @BeforeEach
        void preparaDati() {
            Teacher insegnante = dati.creaInsegnantePredefinito();
            dati.creaCorsoPredefinito(insegnante);   // non online
            dati.creaCorsoOnline(insegnante);        // online
        }

        @Test
        @WithMockUser(roles = "STUDENT")
        @DisplayName("Il filtro online restituisce solo i corsi online e riceve 200 OK")
        void soloCorsiOnline_200() throws Exception {
            mockMvc.perform(get("/api/courses/online"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].online").value(true));
        }

        @Test
        @WithMockUser(roles = "STUDENT")
        @DisplayName("Tutti i corsi restituiti dal filtro online hanno il flag online a true")
        void verificaFlagOnline() throws Exception {
            mockMvc.perform(get("/api/courses/online"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].online").value(true))
                    .andExpect(jsonPath("$[0].codiceCorso").value("C002"));
        }
    }

    // ========== RICERCA PER CODICE ==========

    @Nested
    @DisplayName("GET /api/courses/{codiceCorso} - Ricerca per codice")
    class RicercaPerCodice {

        @BeforeEach
        void preparaDati() {
            Teacher insegnante = dati.creaInsegnantePredefinito();
            dati.creaCorsoPredefinito(insegnante);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Il corso viene trovato per codice e riceve 200 OK")
        void corsoTrovato_200() throws Exception {
            mockMvc.perform(get("/api/courses/C001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.codiceCorso").value("C001"))
                    .andExpect(jsonPath("$.nomeInsegnante").value("Luigi Verdi"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("La risposta JSON contiene il nome dell'insegnante associato al corso")
        void verificaNomeInsegnante() throws Exception {
            mockMvc.perform(get("/api/courses/C001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nomeInsegnante").value("Luigi Verdi"))
                    .andExpect(jsonPath("$.nome").value("Pianoforte Base"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Un corso inesistente restituisce 404 Not Found")
        void corsoNonTrovato_404() throws Exception {
            mockMvc.perform(get("/api/courses/INESISTENTE"))
                    .andExpect(status().isNotFound());
        }
    }

    // ========== AGGIORNAMENTO ==========

    @Nested
    @DisplayName("PUT /api/courses/{codiceCorso} - Aggiornamento corso")
    class AggiornamentoCorso {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("L'admin aggiorna un corso con successo e riceve 200 OK")
        void adminAggiorna_200() throws Exception {
            Teacher insegnante = dati.creaInsegnantePredefinito();
            dati.creaCorsoPredefinito(insegnante);

            CourseRequest request = new CourseRequest(
                    "C001", "Pianoforte Avanzato",
                    LocalDate.of(2026, 3, 1), LocalDate.of(2026, 9, 30),
                    30.0, 60, false, Livello.AVANZATO);

            mockMvc.perform(put("/api/courses/C001")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nome").value("Pianoforte Avanzato"))
                    .andExpect(jsonPath("$.livello").value("AVANZATO"));
        }

        @Test
        @WithMockUser(roles = "STUDENT")
        @DisplayName("Lo studente non puo' aggiornare un corso e riceve 403 Forbidden")
        void studenteAggiorna_403() throws Exception {
            CourseRequest request = new CourseRequest(
                    "C001", "Test", LocalDate.of(2026, 3, 1),
                    LocalDate.of(2026, 6, 30), 25.0, 40, false, null);

            mockMvc.perform(put("/api/courses/C001")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("L'aggiornamento di un corso inesistente restituisce 404 Not Found")
        void aggiornamentoNonTrovato_404() throws Exception {
            CourseRequest request = new CourseRequest(
                    "INESISTENTE", "Test", LocalDate.of(2026, 3, 1),
                    LocalDate.of(2026, 6, 30), 25.0, 40, false, null);

            mockMvc.perform(put("/api/courses/INESISTENTE")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    // ========== ELIMINAZIONE ==========

    @Nested
    @DisplayName("DELETE /api/courses/{codiceCorso} - Eliminazione corso")
    class EliminazioneCorso {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("L'admin elimina un corso con successo e riceve 204 No Content")
        void adminElimina_204() throws Exception {
            Teacher insegnante = dati.creaInsegnantePredefinito();
            dati.creaCorsoPredefinito(insegnante);

            mockMvc.perform(delete("/api/courses/C001"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(roles = "TEACHER")
        @DisplayName("L'insegnante non puo' eliminare corsi e riceve 403 Forbidden")
        void insegnanteElimina_403() throws Exception {
            mockMvc.perform(delete("/api/courses/C001"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("L'eliminazione di un corso inesistente restituisce 404 Not Found")
        void eliminazioneNonTrovato_404() throws Exception {
            mockMvc.perform(delete("/api/courses/INESISTENTE"))
                    .andExpect(status().isNotFound());
        }
    }

    // ========== LEZIONI ==========

    @Nested
    @DisplayName("POST /api/courses/{codiceCorso}/lessons - Aggiunta lezione")
    class AggiuntaLezione {

        @Test
        @WithMockUser(roles = "TEACHER")
        @DisplayName("L'insegnante aggiunge una lezione con successo e riceve 201 Created")
        void insegnanteAggiungeLezione_201() throws Exception {
            Teacher insegnante = dati.creaInsegnantePredefinito();
            dati.creaCorsoPredefinito(insegnante);

            LessonRequest request = new LessonRequest(
                    1, LocalDate.of(2026, 3, 5), LocalTime.of(10, 0),
                    60, "Aula 1", "Introduzione al pianoforte");

            mockMvc.perform(post("/api/courses/C001/lessons")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.numero").value(1))
                    .andExpect(jsonPath("$.argomento").value("Introduzione al pianoforte"));
        }

        @Test
        @WithMockUser(roles = "TEACHER")
        @DisplayName("La lezione creata contiene tutti i campi nel body di risposta")
        void verificaBodyLezione() throws Exception {
            Teacher insegnante = dati.creaInsegnantePredefinito();
            dati.creaCorsoPredefinito(insegnante);

            LessonRequest request = new LessonRequest(
                    1, LocalDate.of(2026, 3, 5), LocalTime.of(10, 0),
                    60, "Aula 1", "Introduzione al pianoforte");

            mockMvc.perform(post("/api/courses/C001/lessons")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.numero").value(1))
                    .andExpect(jsonPath("$.durata").value(60))
                    .andExpect(jsonPath("$.aula").value("Aula 1"))
                    .andExpect(jsonPath("$.argomento").value("Introduzione al pianoforte"));
        }

        @Test
        @WithMockUser(roles = "STUDENT")
        @DisplayName("Lo studente non puo' aggiungere lezioni e riceve 403 Forbidden")
        void studenteAggiungeLezione_403() throws Exception {
            LessonRequest request = new LessonRequest(
                    1, LocalDate.of(2026, 3, 5), LocalTime.of(10, 0),
                    60, "Aula 1", "Test");

            mockMvc.perform(post("/api/courses/C001/lessons")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    // ========== STRUMENTI ==========

    @Nested
    @DisplayName("POST /api/courses/{codiceCorso}/instruments/{codiceStrumento} - Aggiunta strumento")
    class AggiuntaStrumento {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("L'admin aggiunge uno strumento al corso con successo e riceve 200 OK")
        void adminAggiungeStrumento_200() throws Exception {
            Teacher insegnante = dati.creaInsegnantePredefinito();
            dati.creaCorsoPredefinito(insegnante);
            dati.creaStrumentoPredefinito();

            mockMvc.perform(post("/api/courses/C001/instruments/S001"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("L'admin aggiunge un secondo strumento al corso con successo")
        void adminAggiungeSecondoStrumento_200() throws Exception {
            Teacher insegnante = dati.creaInsegnantePredefinito();
            dati.creaCorsoPredefinito(insegnante);
            dati.creaStrumentoPredefinito();
            dati.creaSecondoStrumento();

            mockMvc.perform(post("/api/courses/C001/instruments/S001"))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/api/courses/C001/instruments/S002"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "STUDENT")
        @DisplayName("Lo studente non puo' aggiungere strumenti ai corsi e riceve 403 Forbidden")
        void studenteAggiungeStrumento_403() throws Exception {
            mockMvc.perform(post("/api/courses/C001/instruments/S001"))
                    .andExpect(status().isForbidden());
        }
    }
}
