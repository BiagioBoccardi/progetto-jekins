package com.scuoladimusica.model.dto.response;

import com.scuoladimusica.model.entity.Livello;
import java.util.List;

public record StudentReportResponse(
        String studente,
        Livello livello,
        int numCorsi,
        double mediaVoti,
        List<String> corsi
) {}
