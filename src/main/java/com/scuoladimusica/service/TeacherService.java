package com.scuoladimusica.service;

import com.scuoladimusica.exception.BusinessRuleException;
import com.scuoladimusica.exception.DuplicateResourceException;
import com.scuoladimusica.exception.ResourceNotFoundException;
import com.scuoladimusica.model.dto.request.TeacherRequest;
import com.scuoladimusica.model.dto.response.TeacherResponse;
import com.scuoladimusica.model.entity.Course;
import com.scuoladimusica.model.entity.Teacher;
import com.scuoladimusica.repository.CourseRepository;
import com.scuoladimusica.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TeacherService {

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private CourseRepository courseRepository;

    public TeacherResponse createTeacher(TeacherRequest request) {
        if (teacherRepository.existsByMatricolaInsegnante(request.matricolaInsegnante())) {
            throw new DuplicateResourceException("Matricola insegnante già esistente: " + request.matricolaInsegnante());
        }
        if (teacherRepository.existsByCf(request.cf())) {
            throw new DuplicateResourceException("Codice fiscale già esistente: " + request.cf());
        }
        if (request.stipendio() <= 0) {
            throw new BusinessRuleException("Lo stipendio deve essere maggiore di 0");
        }

        Teacher teacher = new Teacher();
        teacher.setMatricolaInsegnante(request.matricolaInsegnante());
        teacher.setCf(request.cf());
        teacher.setNome(request.nome());
        teacher.setCognome(request.cognome());
        teacher.setDataNascita(request.dataNascita());
        teacher.setTelefono(request.telefono());
        teacher.setStipendio(request.stipendio());
        teacher.setSpecializzazione(request.specializzazione());
        teacher.setAnniEsperienza(request.anniEsperienza());

        Teacher saved = teacherRepository.save(teacher);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public TeacherResponse getTeacherByMatricola(String matricola) {
        Teacher teacher = teacherRepository.findByMatricolaInsegnante(matricola)
                .orElseThrow(() -> new ResourceNotFoundException("Insegnante non trovato con matricola: " + matricola));
        return toResponse(teacher);
    }

    @Transactional(readOnly = true)
    public List<TeacherResponse> getAllTeachers() {
        return teacherRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public TeacherResponse updateTeacher(String matricola, TeacherRequest request) {
        Teacher teacher = teacherRepository.findByMatricolaInsegnante(matricola)
                .orElseThrow(() -> new ResourceNotFoundException("Insegnante non trovato con matricola: " + matricola));

        teacher.setNome(request.nome());
        teacher.setCognome(request.cognome());
        teacher.setTelefono(request.telefono());
        teacher.setStipendio(request.stipendio());
        teacher.setSpecializzazione(request.specializzazione());
        teacher.setAnniEsperienza(request.anniEsperienza());

        Teacher saved = teacherRepository.save(teacher);
        return toResponse(saved);
    }

    public void deleteTeacher(String matricola) {
        Teacher teacher = teacherRepository.findByMatricolaInsegnante(matricola)
                .orElseThrow(() -> new ResourceNotFoundException("Insegnante non trovato con matricola: " + matricola));
        teacherRepository.delete(teacher);
    }

    public void assignCourse(String matricolaInsegnante, String codiceCorso) {
        Teacher teacher = teacherRepository.findByMatricolaInsegnante(matricolaInsegnante)
                .orElseThrow(() -> new ResourceNotFoundException("Insegnante non trovato con matricola: " + matricolaInsegnante));

        Course course = courseRepository.findByCodiceCorso(codiceCorso)
                .orElseThrow(() -> new ResourceNotFoundException("Corso non trovato con codice: " + codiceCorso));

        if (course.getTeacher() != null) {
            throw new BusinessRuleException("Il corso è già assegnato a un insegnante");
        }

        // AGGIUNTA: Aggiorniamo ENTRAMBI i lati della relazione
        course.setTeacher(teacher);
        teacher.getCourses().add(course); 
        
        courseRepository.save(course);
        // Salviamo anche il teacher per assicurarci che la lista in memoria sia aggiornata
        teacherRepository.save(teacher); 
    }

    private TeacherResponse toResponse(Teacher teacher) {
        return new TeacherResponse(
                teacher.getId(),
                teacher.getMatricolaInsegnante(),
                teacher.getCf(),
                teacher.getNome(),
                teacher.getCognome(),
                teacher.getNomeCompleto(),
                teacher.getDataNascita(),
                teacher.getTelefono(),
                teacher.getStipendio(),
                teacher.getSpecializzazione(),
                teacher.getAnniEsperienza(),
                teacher.getCourses().size()
        );
    }
}