package com.scuoladimusica.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.scuoladimusica.model.entity.Course;
import com.scuoladimusica.model.entity.Livello;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByCodiceCorso(String codiceCorso);

    boolean existsByCodiceCorso(String codiceCorso);

    List<Course> findByOnlineTrue();

    List<Course> findByLivello(Livello livello);

    List<Course> findByTeacherId(Long teacherId);
}