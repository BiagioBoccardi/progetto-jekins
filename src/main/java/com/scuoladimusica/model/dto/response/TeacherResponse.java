package com.scuoladimusica.model.dto.response;

import java.time.LocalDate;

public record TeacherResponse(
        Long id,
        String matricolaInsegnante,
        String cf,
        String nome,
        String cognome,
        String nomeCompleto,
        LocalDate dataNascita,
        String telefono,
        Double stipendio,
        String specializzazione,
        int anniEsperienza,
        int numeroCorsiTenuti
) {}
