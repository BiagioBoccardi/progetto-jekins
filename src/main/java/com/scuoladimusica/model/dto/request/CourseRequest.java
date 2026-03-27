package com.scuoladimusica.model.dto.request;

import com.scuoladimusica.model.entity.Livello;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record CourseRequest(
        @NotBlank(message = "Il codice corso non può essere vuoto")
        String codiceCorso,

        @NotBlank(message = "Il nome del corso non può essere vuoto")
        String nome,

        @NotNull(message = "La data di inizio è obbligatoria")
        LocalDate dataInizio,

        @NotNull(message = "La data di fine è obbligatoria")
        LocalDate dataFine,

        @NotNull(message = "Il costo orario è obbligatorio")
        @Positive(message = "Il costo orario deve essere positivo")
        Double costoOrario,

        @NotNull(message = "Il totale ore è obbligatorio")
        @Positive(message = "Il totale ore deve essere positivo")
        Integer totaleOre,

        boolean online,

        Livello livello
) {}
