package com.scuoladimusica.model.dto.response;

public record EnrollmentResponse(
        Long id,
        String matricolaStudente,
        String nomeStudente,
        String codiceCorso,
        String nomeCorso,
        int annoIscrizione,
        Integer votoFinale
) {}
