package com.scuoladimusica.repository;

import com.scuoladimusica.model.entity.Instrument;
import com.scuoladimusica.model.entity.Loan;
import com.scuoladimusica.model.entity.TipoStrumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository per la gestione degli strumenti musicali.
 *
 * TODO: Aggiungere i seguenti metodi:
 *
 * 1. Metodo per trovare uno strumento tramite il suo codice strumento.
 *    Deve restituire un Optional<Instrument>.
 *
 * 2. Metodo per verificare se esiste uno strumento con un dato codice.
 *    Deve restituire un boolean.
 *
 * 3. Metodo per trovare tutti gli strumenti di un certo tipo (TipoStrumento).
 *    Deve restituire una List<Instrument>.
 *
 * 4. Metodo JPQL personalizzato per trovare gli strumenti disponibili
 *    (cioè quelli che NON hanno un prestito attivo con dataFine IS NULL).
 *    Deve restituire una List<Instrument>.
 *
 *    SUGGERIMENTO: usare l'annotazione @Query con JPQL:
 *    "SELECT i FROM Instrument i WHERE NOT EXISTS
 *     (SELECT l FROM Loan l WHERE l.instrument = i AND l.dataFine IS NULL)"
 */
@Repository
public interface InstrumentRepository extends JpaRepository<Instrument, Long> {

    Optional<Instrument> findByCodiceStrumento(String codiceStrumento);

    boolean existsByCodiceStrumento(String codiceStrumento);

    List<Instrument> findByTipoStrumento(TipoStrumento tipoStrumento);

    @Query("SELECT i FROM Instrument i WHERE NOT EXISTS (SELECT l FROM Loan l WHERE l.instrument = i AND l.dataFine IS NULL)")
    List<Instrument> findAvailable();
}
