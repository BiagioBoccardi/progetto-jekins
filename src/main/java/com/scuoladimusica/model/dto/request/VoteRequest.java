package com.scuoladimusica.model.dto.request;

import jakarta.validation.constraints.*;

public record VoteRequest(
        @Min(value = 18, message = "Il voto minimo è 18")
        @Max(value = 30, message = "Il voto massimo è 30")
        int voto
) {}
