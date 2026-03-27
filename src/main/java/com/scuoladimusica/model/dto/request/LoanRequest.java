package com.scuoladimusica.model.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record LoanRequest(
        @NotBlank(message = "La matricola studente è obbligatoria")
        String matricolaStudente,

        @NotNull(message = "La data di inizio è obbligatoria")
        LocalDate dataInizio
) {}
