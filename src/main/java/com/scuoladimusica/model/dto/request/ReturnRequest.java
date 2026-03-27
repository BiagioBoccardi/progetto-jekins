package com.scuoladimusica.model.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record ReturnRequest(
        @NotNull(message = "La data di restituzione è obbligatoria")
        LocalDate dataFine
) {}
