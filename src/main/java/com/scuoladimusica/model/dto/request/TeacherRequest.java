package com.scuoladimusica.model.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record TeacherRequest(
        @NotBlank(message = "La matricola insegnante non può essere vuota")
        String matricolaInsegnante,

        @NotBlank(message = "Il codice fiscale non può essere vuoto")
        @Size(min = 16, max = 16, message = "Il codice fiscale deve essere di 16 caratteri")
        String cf,

        @NotBlank(message = "Il nome non può essere vuoto")
        String nome,

        @NotBlank(message = "Il cognome non può essere vuoto")
        String cognome,

        @NotNull(message = "La data di nascita è obbligatoria")
        @Past(message = "La data di nascita deve essere nel passato")
        LocalDate dataNascita,

        String telefono,

        @NotNull(message = "Lo stipendio è obbligatorio")
        @Positive(message = "Lo stipendio deve essere positivo")
        Double stipendio,

        @NotBlank(message = "La specializzazione non può essere vuota")
        String specializzazione,

        @Min(value = 0, message = "Gli anni di esperienza non possono essere negativi")
        int anniEsperienza
) {}
