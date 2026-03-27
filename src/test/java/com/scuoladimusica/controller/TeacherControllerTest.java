package com.scuoladimusica.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.scuoladimusica.TestDataFactory;
import com.scuoladimusica.model.dto.request.TeacherRequest;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TeacherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestDataFactory dati;

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    // ========== CREAZIONE ==========

    @Nested
    @DisplayName("POST /api/teachers - Creazione insegnante")
    class CreazioneInsegnante {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("L'admin crea un insegnante con successo e riceve 201 Created")
        void adminCreaInsegnante_201() throws Exception {
            TeacherRequest request = new TeacherRequest(
                    "I001", "VRDLGU80A01H501Z", "Luigi", "Verdi",
                    LocalDate.of(1980, 1, 1), "3331234567", 2500.0, "Pianoforte", 15);

            mockMvc.perform(post("/api/teachers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.matricolaInsegnante").value("I001"))
                    .andExpect(jsonPath("$.nomeCompleto").value("Luigi Verdi"))
                    .andExpect(jsonPath("$.specializzazione").value("Pianoforte"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("L'admin crea un insegnante e il body contiene tutti i campi attesi")
        void adminCreaInsegnante_verificaBody() throws Exception {
            TeacherRequest request = new TeacherRequest(
                    "I001", "VRDLGU80A01H501Z", "Luigi", "Verdi",
                    LocalDate.of(1980, 1, 1), "3331234567", 2500.0, "Pianoforte", 15);

            mockMvc.perform(post("/api/teachers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.matricolaInsegnante").value("I001"))
                    .andExpect(jsonPath("$.cf").value("VRDLGU80A01H501Z"))
                    .andExpect(jsonPath("$.nome").value("Luigi"))
                    .andExpect(jsonPath("$.cognome").value("Verdi"))
                    .andExpect(jsonPath("$.stipendio").value(2500.0))
                    .andExpect(jsonPath("$.anniEsperienza").value(15));
        }

        @Test
        @WithMockUser(roles = "TEACHER")
        @DisplayName("L'insegnante non puo' creare altri insegnanti e riceve 403 Forbidden")
        void insegnanteCreaInsegnante_403() throws Exception {
            TeacherRequest request = new TeacherRequest(
                    "I001", "VRDLGU80A01H501Z", "Luigi", "Verdi",
                    LocalDate.of(1980, 1, 1), null, 2500.0, "Pianoforte", 15);

            mockMvc.perform(post("/api/teachers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "STUDENT")
        @DisplayName("Lo studente non puo' creare insegnanti e riceve 403 Forbidden")
        void studenteCreaInsegnante_403() throws Exception {
            TeacherRequest request = new TeacherRequest(
                    "I001", "VRDLGU80A01H501Z", "Luigi", "Verdi",
                    LocalDate.of(1980, 1, 1), null, 2500.0, "Pianoforte", 15);

            mockMvc.perform(post("/api/teachers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Un utente non autenticato non puo' creare insegnanti e riceve 401 Unauthorized")
        void utenteNonAutenticato_401() throws Exception {
            TeacherRequest request = new TeacherRequest(
                    "I001", "VRDLGU80A01H501Z", "Luigi", "Verdi",
                    LocalDate.of(1980, 1, 1), null, 2500.0, "Pianoforte", 15);

            mockMvc.perform(post("/api/teachers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("La creazione con matricola duplicata restituisce 409 Conflict")
        void matricolaDuplicata_409() throws Exception {
            dati.creaInsegnantePredefinito();

            TeacherRequest request = new TeacherRequest(
                    "I001", "ALTROCF12345678AB", "Altro", "Nome",
                    LocalDate.of(1985, 5, 5), null, 2000.0, "Violino", 5);

            mockMvc.perform(post("/api/teachers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("La creazione con nome vuoto restituisce 400 Bad Request")
        void nomeVuoto_400() throws Exception {
            TeacherRequest request = new TeacherRequest(
                    "I001", "VRDLGU80A01H501Z", "", "Verdi",
                    LocalDate.of(1980, 1, 1), null, 2500.0, "Pianoforte", 15);

            mockMvc.perform(post("/api/teachers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ========== LETTURA ==========

    @Nested
    @DisplayName("GET /api/teachers - Recupero insegnanti")
    class RecuperoInsegnanti {

        @BeforeEach
        void preparaDati() {
            dati.creaInsegnantePredefinito();
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("L'admin recupera tutti gli insegnanti e riceve 200 OK")
        void adminRecuperaTutti_200() throws Exception {
            mockMvc.perform(get("/api/teachers"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("L'admin recupera tutti gli insegnanti e la lista contiene i dati corretti")
        void adminRecuperaTutti_verificaDati() throws Exception {
            mockMvc.perform(get("/api/teachers"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].matricolaInsegnante").value("I001"))
                    .andExpect(jsonPath("$[0].nomeCompleto").value("Luigi Verdi"));
        }

        @Test
        @WithMockUser(roles = "STUDENT")
        @DisplayName("Lo studente non puo' vedere la lista insegnanti e riceve 403 Forbidden")
        void studenteRecuperaTutti_403() throws Exception {
            mockMvc.perform(get("/api/teachers"))
                    .andExpect(status().isForbidden());
        }
    }

    // ========== RICERCA PER MATRICOLA ==========

    @Nested
    @DisplayName("GET /api/teachers/{matricola} - Ricerca per matricola")
    class RicercaPerMatricola {

        @BeforeEach
        void preparaDati() {
            dati.creaInsegnantePredefinito();
        }

        @Test
        @WithMockUser(roles = "TEACHER")
        @DisplayName("L'insegnante recupera un insegnante per matricola e riceve 200 OK")
        void insegnanteTrovato_200() throws Exception {
            mockMvc.perform(get("/api/teachers/I001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.matricolaInsegnante").value("I001"))
                    .andExpect(jsonPath("$.nomeCompleto").value("Luigi Verdi"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("La risposta JSON contiene tutti i campi attesi dell'insegnante")
        void verificaBodyCompleto() throws Exception {
            mockMvc.perform(get("/api/teachers/I001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.matricolaInsegnante").value("I001"))
                    .andExpect(jsonPath("$.cf").value("VRDLGU80A01H501Z"))
                    .andExpect(jsonPath("$.nome").value("Luigi"))
                    .andExpect(jsonPath("$.cognome").value("Verdi"))
                    .andExpect(jsonPath("$.nomeCompleto").value("Luigi Verdi"))
                    .andExpect(jsonPath("$.stipendio").value(2500.0))
                    .andExpect(jsonPath("$.specializzazione").value("Pianoforte"))
                    .andExpect(jsonPath("$.anniEsperienza").value(15));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Un insegnante inesistente restituisce 404 Not Found")
        void insegnanteNonTrovato_404() throws Exception {
            mockMvc.perform(get("/api/teachers/INESISTENTE"))
                    .andExpect(status().isNotFound());
        }
    }

    // ========== AGGIORNAMENTO ==========

    @Nested
    @DisplayName("PUT /api/teachers/{matricola} - Aggiornamento insegnante")
    class AggiornamentoInsegnante {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("L'admin aggiorna un insegnante con successo e riceve 200 OK")
        void adminAggiorna_200() throws Exception {
            dati.creaInsegnantePredefinito();

            TeacherRequest request = new TeacherRequest(
                    "I001", "VRDLGU80A01H501Z", "Luigi", "Verdi",
                    LocalDate.of(1980, 1, 1), "3339876543", 3000.0, "Pianoforte e Composizione", 16);

            mockMvc.perform(put("/api/teachers/I001")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.stipendio").value(3000.0))
                    .andExpect(jsonPath("$.specializzazione").value("Pianoforte e Composizione"));
        }

        @Test
        @WithMockUser(roles = "TEACHER")
        @DisplayName("L'insegnante non puo' aggiornare e riceve 403 Forbidden")
        void insegnanteAggiorna_403() throws Exception {
            TeacherRequest request = new TeacherRequest(
                    "I001", "VRDLGU80A01H501Z", "Test", "Test",
                    LocalDate.of(1980, 1, 1), null, 2000.0, "Test", 0);

            mockMvc.perform(put("/api/teachers/I001")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("L'aggiornamento di un insegnante inesistente restituisce 404 Not Found")
        void aggiornamentoNonTrovato_404() throws Exception {
            TeacherRequest request = new TeacherRequest(
                    "INESISTENTE", "VRDLGU80A01H501Z", "Test", "Test",
                    LocalDate.of(1980, 1, 1), null, 2000.0, "Test", 0);

            mockMvc.perform(put("/api/teachers/INESISTENTE")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    // ========== ELIMINAZIONE ==========

    @Nested
    @DisplayName("DELETE /api/teachers/{matricola} - Eliminazione insegnante")
    class EliminazioneInsegnante {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("L'admin elimina un insegnante con successo e riceve 204 No Content")
        void adminElimina_204() throws Exception {
            dati.creaInsegnantePredefinito();

            mockMvc.perform(delete("/api/teachers/I001"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(roles = "TEACHER")
        @DisplayName("L'insegnante non puo' eliminare altri insegnanti e riceve 403 Forbidden")
        void insegnanteElimina_403() throws Exception {
            mockMvc.perform(delete("/api/teachers/I001"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("L'eliminazione di un insegnante inesistente restituisce 404 Not Found")
        void eliminazioneNonTrovato_404() throws Exception {
            mockMvc.perform(delete("/api/teachers/INESISTENTE"))
                    .andExpect(status().isNotFound());
        }
    }

    // ========== ASSEGNAZIONE CORSO ==========

    @Nested
    @DisplayName("POST /api/teachers/{matricola}/courses/{codiceCorso} - Assegnazione corso")
    class AssegnazioneCorso {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("L'admin assegna un corso a un insegnante con successo e riceve 200 OK")
        void adminAssegnaCorso_200() throws Exception {
            dati.creaInsegnantePredefinito();
            dati.creaCorso("C001", "Pianoforte Base",
                    LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30),
                    25.0, 40, false, Livello.PRINCIPIANTE, null);

            mockMvc.perform(post("/api/teachers/I001/courses/C001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Corso assegnato con successo"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Il messaggio di risposta conferma l'assegnazione del corso")
        void assegnaCorso_verificaMessaggio() throws Exception {
            dati.creaInsegnantePredefinito();
            dati.creaCorso("C001", "Pianoforte Base",
                    LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30),
                    25.0, 40, false, Livello.PRINCIPIANTE, null);

            mockMvc.perform(post("/api/teachers/I001/courses/C001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").isString())
                    .andExpect(jsonPath("$.message", containsString("assegnato")));
        }

        @Test
        @WithMockUser(roles = "TEACHER")
        @DisplayName("L'insegnante non puo' assegnare corsi e riceve 403 Forbidden")
        void insegnanteAssegnaCorso_403() throws Exception {
            mockMvc.perform(post("/api/teachers/I001/courses/C001"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("L'assegnazione di un corso gia' assegnato restituisce 400 Bad Request")
        void corsoGiaAssegnato_400() throws Exception {
            Teacher insegnante = dati.creaInsegnantePredefinito();
            dati.creaCorsoPredefinito(insegnante);

            mockMvc.perform(post("/api/teachers/I001/courses/C001"))
                    .andExpect(status().isBadRequest());
        }
    }
}
