package com.scuoladimusica.model.dto.response;

import com.scuoladimusica.model.entity.Livello;
import java.time.LocalDate;

public record StudentResponse(
        Long id,
        String matricola,
        String cf,
        String nome,
        String cognome,
        String nomeCompleto,
        LocalDate dataNascita,
        String telefono,
        Livello livello,
        int numeroCorsiFrequentati,
        double mediaVoti
) {}
