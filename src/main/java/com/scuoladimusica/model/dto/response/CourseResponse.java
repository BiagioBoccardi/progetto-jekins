package com.scuoladimusica.model.dto.response;

import com.scuoladimusica.model.entity.Livello;
import java.time.LocalDate;
import java.util.List;

public record CourseResponse(
        Long id,
        String codiceCorso,
        String nome,
        LocalDate dataInizio,
        LocalDate dataFine,
        Double costoOrario,
        Integer totaleOre,
        double costoTotale,
        long durataGiorni,
        boolean online,
        Livello livello,
        String nomeInsegnante,
        int numeroIscritti,
        List<LessonResponse> lezioni
) {}
