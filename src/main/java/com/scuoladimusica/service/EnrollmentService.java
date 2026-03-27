package com.scuoladimusica.service;

import com.scuoladimusica.exception.BusinessRuleException;
import com.scuoladimusica.exception.DuplicateResourceException;
import com.scuoladimusica.exception.ResourceNotFoundException;
import com.scuoladimusica.model.dto.response.EnrollmentResponse;
import com.scuoladimusica.model.entity.Course;
import com.scuoladimusica.model.entity.Enrollment;
import com.scuoladimusica.model.entity.Student;
import com.scuoladimusica.repository.CourseRepository;
import com.scuoladimusica.repository.EnrollmentRepository;
import com.scuoladimusica.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    public EnrollmentResponse enrollStudent(String matricola, String codiceCorso, int annoIscrizione) {
        Student student = studentRepository.findByMatricola(matricola)
                .orElseThrow(() -> new ResourceNotFoundException("Studente non trovato con matricola: " + matricola));

        Course course = courseRepository.findByCodiceCorso(codiceCorso)
                .orElseThrow(() -> new ResourceNotFoundException("Corso non trovato con codice: " + codiceCorso));

        if (enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
            throw new DuplicateResourceException("Lo studente è già iscritto a questo corso");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setAnnoIscrizione(annoIscrizione);

        Enrollment saved = enrollmentRepository.save(enrollment);
        return toResponse(saved);
    }

    public EnrollmentResponse registerVote(String matricola, String codiceCorso, int voto) {
        Enrollment enrollment = enrollmentRepository.findByStudentMatricolaAndCourseCodiceCorso(matricola, codiceCorso)
                .orElseThrow(() -> new ResourceNotFoundException("Iscrizione non trovata"));

        if (voto < 18 || voto > 30) {
            throw new BusinessRuleException("Il voto deve essere compreso tra 18 e 30");
        }

        enrollment.setVotoFinale(voto);
        Enrollment saved = enrollmentRepository.save(enrollment);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrollmentsByStudent(String matricola) {
        Student student = studentRepository.findByMatricola(matricola)
                .orElseThrow(() -> new ResourceNotFoundException("Studente non trovato con matricola: " + matricola));

        return enrollmentRepository.findByStudentId(student.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrollmentsByCourse(String codiceCorso) {
        Course course = courseRepository.findByCodiceCorso(codiceCorso)
                .orElseThrow(() -> new ResourceNotFoundException("Corso non trovato con codice: " + codiceCorso));

        return enrollmentRepository.findByCourseId(course.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private EnrollmentResponse toResponse(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getStudent().getMatricola(),
                enrollment.getStudent().getNomeCompleto(),
                enrollment.getCourse().getCodiceCorso(),
                enrollment.getCourse().getNome(),
                enrollment.getAnnoIscrizione(),
                enrollment.getVotoFinale()
        );
    }
}
