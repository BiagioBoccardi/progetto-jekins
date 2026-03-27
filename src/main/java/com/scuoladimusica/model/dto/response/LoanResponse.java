package com.scuoladimusica.model.dto.response;

import java.time.LocalDate;

public record LoanResponse(
        Long id,
        String codiceStrumento,
        String nomeStrumento,
        String matricolaStudente,
        String nomeStudente,
        LocalDate dataInizio,
        LocalDate dataFine
) {}
