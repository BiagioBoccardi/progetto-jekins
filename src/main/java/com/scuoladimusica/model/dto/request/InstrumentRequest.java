package com.scuoladimusica.model.dto.request;

import com.scuoladimusica.model.entity.TipoStrumento;
import jakarta.validation.constraints.*;

public record InstrumentRequest(
        @NotBlank(message = "Il codice strumento non può essere vuoto")
        String codiceStrumento,

        @NotBlank(message = "Il nome dello strumento non può essere vuoto")
        String nome,

        @NotNull(message = "Il tipo strumento è obbligatorio")
        TipoStrumento tipoStrumento,

        String marca,

        Integer annoProduzione,

        // Campi specifici per strumenti a corda
        Integer numeroCorde,
        String tipoCorde,
        String materiale,

        // Campi specifici per percussioni
        String tipoPelle,
        Double diametro
) {}
