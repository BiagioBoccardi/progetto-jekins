package com.scuoladimusica.model.dto.request;

import jakarta.validation.constraints.*;

public record EnrollmentRequest(
        @NotBlank(message = "La matricola studente è obbligatoria")
        String matricolaStudente,

        @NotBlank(message = "Il codice corso è obbligatorio")
        String codiceCorso,

        @Positive(message = "L'anno di iscrizione deve essere positivo")
        int annoIscrizione
) {}
