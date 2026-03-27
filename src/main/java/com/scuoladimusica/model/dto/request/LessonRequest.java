package com.scuoladimusica.model.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;

public record LessonRequest(
        @Positive(message = "Il numero lezione deve essere positivo")
        int numero,

        @NotNull(message = "La data è obbligatoria")
        LocalDate data,

        @NotNull(message = "L'ora di inizio è obbligatoria")
        LocalTime oraInizio,

        @Positive(message = "La durata deve essere positiva")
        int durata,

        String aula,
        String argomento
) {}
