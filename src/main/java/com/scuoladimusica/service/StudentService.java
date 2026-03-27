package com.scuoladimusica.service;

import com.scuoladimusica.exception.DuplicateResourceException;
import com.scuoladimusica.exception.ResourceNotFoundException;
import com.scuoladimusica.model.dto.request.StudentRequest;
import com.scuoladimusica.model.dto.response.StudentReportResponse;
import com.scuoladimusica.model.dto.response.StudentResponse;
import com.scuoladimusica.model.entity.Livello;
import com.scuoladimusica.model.entity.Student;
import com.scuoladimusica.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    public StudentResponse createStudent(StudentRequest request) {
        if (studentRepository.existsByMatricola(request.matricola())) {
            throw new DuplicateResourceException("Matricola già esistente: " + request.matricola());
        }
        if (studentRepository.existsByCf(request.cf())) {
            throw new DuplicateResourceException("Codice fiscale già esistente: " + request.cf());
        }

        Student student = new Student();
        student.setMatricola(request.matricola());
        student.setCf(request.cf());
        student.setNome(request.nome());
        student.setCognome(request.cognome());
        student.setDataNascita(request.dataNascita());
        student.setTelefono(request.telefono());
        student.setLivello(request.livello() != null ? request.livello() : Livello.PRINCIPIANTE);

        Student saved = studentRepository.save(student);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public StudentResponse getStudentByMatricola(String matricola) {
        Student student = studentRepository.findByMatricola(matricola)
                .orElseThrow(() -> new ResourceNotFoundException("Studente non trovato: " + matricola));
        return toResponse(student);
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> getAllStudents() {
        return studentRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> getStudentsByLivello(Livello livello) {
        return studentRepository.findByLivello(livello).stream().map(this::toResponse).toList();
    }

    public StudentResponse updateStudent(String matricola, StudentRequest request) {
        Student student = studentRepository.findByMatricola(matricola)
                .orElseThrow(() -> new ResourceNotFoundException("Studente non trovato: " + matricola));
        student.setNome(request.nome());
        student.setCognome(request.cognome());
        student.setTelefono(request.telefono());
        student.setLivello(request.livello() != null ? request.livello() : student.getLivello());
        return toResponse(studentRepository.save(student));
    }

    public void deleteStudent(String matricola) {
        Student student = studentRepository.findByMatricola(matricola)
                .orElseThrow(() -> new ResourceNotFoundException("Studente non trovato: " + matricola));
        studentRepository.delete(student);
    }

    @Transactional(readOnly = true)
    public StudentReportResponse getStudentReport(String matricola) {
        Student student = studentRepository.findByMatricola(matricola)
                .orElseThrow(() -> new ResourceNotFoundException("Studente non trovato: " + matricola));

        List<String> corsi = student.getEnrollments().stream()
                .map(e -> e.getCourse().getNome())
                .collect(Collectors.toList());

        double media = student.getEnrollments().stream()
                .filter(e -> e.getVotoFinale() != null)
                .mapToDouble(e -> e.getVotoFinale())
                .average()
                .orElse(0.0);

        return new StudentReportResponse(
                student.getNomeCompleto(),
                student.getLivello(),
                student.getEnrollments().size(),
                media,
                corsi
        );
    }

    private StudentResponse toResponse(Student student) {
        int numCorsi = student.getEnrollments() != null ? student.getEnrollments().size() : 0;
        double media = 0.0;
        if (numCorsi > 0) {
            media = student.getEnrollments().stream()
                    .filter(e -> e.getVotoFinale() != null)
                    .mapToDouble(e -> e.getVotoFinale())
                    .average().orElse(0.0);
        }
        return new StudentResponse(
                student.getId(), student.getMatricola(), student.getCf(),
                student.getNome(), student.getCognome(), student.getNomeCompleto(),
                student.getDataNascita(), student.getTelefono(), student.getLivello(),
                numCorsi, media
        );
    }
}