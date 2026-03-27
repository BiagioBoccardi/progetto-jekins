package com.scuoladimusica.model.dto.response;

import com.scuoladimusica.model.entity.TipoStrumento;

public record InstrumentResponse(
        Long id,
        String codiceStrumento,
        String nome,
        TipoStrumento tipoStrumento,
        String marca,
        Integer annoProduzione,
        boolean disponibile
) {}
