package com.scuoladimusica.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.scuoladimusica.TestDataFactory;
import com.scuoladimusica.model.dto.request.StudentRequest;
import com.scuoladimusica.model.entity.*;
import com.scuoladimusica.repository.EnrollmentRepository;
import jakarta.persistence.EntityManager;
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
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestDataFactory dati;

    @Autowired
    private EntityManager entityManager;

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Nested
    @DisplayName("POST /api/students - Creazione studente")
    class CreazioneStudente {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("La creazione con matricola duplicata restituisce 409 Conflict")
        void matricolaDuplicata_409() throws Exception {
            dati.creaStudentePredefinito();

            // CF di 16 caratteri esatti
            StudentRequest request = new StudentRequest(
                    "M001", "ALTROCF12345678A", "Altro", "Nome",
                    LocalDate.of(1985, 2, 2), null, null);

            mockMvc.perform(post("/api/students")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("GET /api/students/{matricola}/report - Report studente")
    class ReportStudente {

        @Test
        @WithMockUser(roles = "ADMIN")
        void adminReportConCorsi_200() throws Exception {
            Student studente = dati.creaStudentePredefinito();
            Teacher insegnante = dati.creaInsegnantePredefinito();
            Course corso = dati.creaCorsoPredefinito(insegnante);
            dati.creaIscrizioneConVoto(studente, corso, 2026, 28);

            entityManager.flush();
            entityManager.clear();

            mockMvc.perform(get("/api/students/M001/report"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.numCorsi").value(1));
        }
    }
}