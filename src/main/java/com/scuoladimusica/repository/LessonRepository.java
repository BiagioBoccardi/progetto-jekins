package com.scuoladimusica.repository;

import com.scuoladimusica.model.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository per la gestione delle lezioni.
 *
 * TODO: Aggiungere i seguenti metodi:
 *
 * 1. Metodo per trovare tutte le lezioni di un corso (tramite course_id).
 *    Deve restituire una List<Lesson>.
 *
 * 2. Metodo per verificare se esiste una lezione con un dato numero
 *    all'interno di un corso specifico.
 *    Deve restituire un boolean.
 *    SUGGERIMENTO: existsByCourseIdAndNumero(Long courseId, int numero)
 */
@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findByCourseId(Long courseId);

    boolean existsByCourseIdAndNumero(Long courseId, int numero);
}
