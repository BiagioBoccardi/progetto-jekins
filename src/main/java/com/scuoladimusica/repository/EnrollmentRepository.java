package com.scuoladimusica.repository;

import com.scuoladimusica.model.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository per la gestione delle iscrizioni (studente-corso).
 *
 * TODO: Aggiungere i seguenti metodi:
 *
 * 1. Metodo per trovare tutte le iscrizioni di uno studente (tramite student_id).
 *    Deve restituire una List<Enrollment>.
 *
 * 2. Metodo per trovare tutte le iscrizioni per un corso (tramite course_id).
 *    Deve restituire una List<Enrollment>.
 *
 * 3. Metodo per verificare se esiste un'iscrizione per una coppia studente-corso.
 *    Deve restituire un boolean.
 *    SUGGERIMENTO: existsByStudentIdAndCourseId(Long studentId, Long courseId)
 *
 * 4. Metodo per trovare un'iscrizione tramite la matricola dello studente
 *    e il codice del corso (navigando le relazioni).
 *    Deve restituire un Optional<Enrollment>.
 *    SUGGERIMENTO: findByStudentMatricolaAndCourseCodiceCorso(...)
 */
@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByStudentId(Long studentId);

    List<Enrollment> findByCourseId(Long courseId);

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    Optional<Enrollment> findByStudentMatricolaAndCourseCodiceCorso(String matricola, String codiceCorso);
}
