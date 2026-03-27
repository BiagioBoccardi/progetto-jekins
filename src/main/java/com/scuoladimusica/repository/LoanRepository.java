package com.scuoladimusica.repository;

import com.scuoladimusica.model.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository per la gestione dei prestiti strumenti.
 *
 * TODO: Aggiungere i seguenti metodi:
 *
 * 1. Metodo per trovare tutti i prestiti di uno studente (tramite student_id).
 *    Deve restituire una List<Loan>.
 *
 * 2. Metodo per trovare tutti i prestiti di uno strumento (tramite instrument_id).
 *    Deve restituire una List<Loan>.
 *
 * 3. Metodo per trovare il prestito attivo di uno strumento
 *    (cioè quello con dataFine IS NULL per un dato instrument_id).
 *    Deve restituire un Optional<Loan>.
 *    SUGGERIMENTO: findByInstrumentIdAndDataFineIsNull(Long instrumentId)
 *
 * 4. Metodo per verificare se esiste un prestito attivo per uno strumento.
 *    Deve restituire un boolean.
 *    SUGGERIMENTO: existsByInstrumentIdAndDataFineIsNull(Long instrumentId)
 */
@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByStudentId(Long studentId);

    List<Loan> findByInstrumentId(Long instrumentId);

    Optional<Loan> findByInstrumentIdAndDataFineIsNull(Long instrumentId);

    boolean existsByInstrumentIdAndDataFineIsNull(Long instrumentId);
}
