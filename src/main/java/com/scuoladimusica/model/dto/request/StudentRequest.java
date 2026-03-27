package com.scuoladimusica.model.dto.request;

import com.scuoladimusica.model.entity.Livello;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record StudentRequest(
        @NotBlank(message = "La matricola non può essere vuota")
        String matricola,

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

        Livello livello
) {}
