package com.scuoladimusica.model.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

public record LessonResponse(
        Long id,
        int numero,
        LocalDate data,
        LocalTime oraInizio,
        int durata,
        String aula,
        String argomento
) {}
