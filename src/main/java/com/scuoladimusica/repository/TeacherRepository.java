package com.scuoladimusica.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.scuoladimusica.model.entity.Teacher;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    Optional<Teacher> findByMatricolaInsegnante(String matricolaInsegnante);

    boolean existsByMatricolaInsegnante(String matricolaInsegnante);

    boolean existsByCf(String cf);
}
