package com.scuoladimusica.service;

import com.scuoladimusica.exception.BusinessRuleException;
import com.scuoladimusica.exception.DuplicateResourceException;
import com.scuoladimusica.exception.ResourceNotFoundException;
import com.scuoladimusica.model.dto.request.CourseRequest;
import com.scuoladimusica.model.dto.request.LessonRequest;
import com.scuoladimusica.model.dto.response.CourseResponse;
import com.scuoladimusica.model.dto.response.LessonResponse;
import com.scuoladimusica.model.entity.Course;
import com.scuoladimusica.model.entity.Instrument;
import com.scuoladimusica.model.entity.Lesson;
import com.scuoladimusica.model.entity.Livello;
import com.scuoladimusica.repository.CourseRepository;
import com.scuoladimusica.repository.InstrumentRepository;
import com.scuoladimusica.repository.LessonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private InstrumentRepository instrumentRepository;

    public CourseResponse createCourse(CourseRequest request) {
        if (courseRepository.existsByCodiceCorso(request.codiceCorso())) {
            throw new DuplicateResourceException("Codice corso già esistente: " + request.codiceCorso());
        }
        if (!request.dataFine().isAfter(request.dataInizio())) {
            throw new BusinessRuleException("La data di fine deve essere successiva alla data di inizio");
        }

        Course course = new Course();
        course.setCodiceCorso(request.codiceCorso());
        course.setNome(request.nome());
        course.setDataInizio(request.dataInizio());
        course.setDataFine(request.dataFine());
        course.setCostoOrario(request.costoOrario());
        course.setTotaleOre(request.totaleOre());
        course.setOnline(request.online());
        course.setLivello(request.livello() != null ? request.livello() : Livello.PRINCIPIANTE);

        Course saved = courseRepository.save(course);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourseByCode(String codiceCorso) {
        Course course = courseRepository.findByCodiceCorso(codiceCorso)
                .orElseThrow(() -> new ResourceNotFoundException("Corso non trovato con codice: " + codiceCorso));
        return toResponse(course);
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getOnlineCourses() {
        return courseRepository.findByOnlineTrue()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public CourseResponse updateCourse(String codiceCorso, CourseRequest request) {
        Course course = courseRepository.findByCodiceCorso(codiceCorso)
                .orElseThrow(() -> new ResourceNotFoundException("Corso non trovato con codice: " + codiceCorso));

        course.setNome(request.nome());
        course.setDataInizio(request.dataInizio());
        course.setDataFine(request.dataFine());
        course.setCostoOrario(request.costoOrario());
        course.setTotaleOre(request.totaleOre());
        course.setOnline(request.online());
        course.setLivello(request.livello() != null ? request.livello() : course.getLivello());

        if (!course.getDataFine().isAfter(course.getDataInizio())) {
            throw new BusinessRuleException("La data di fine deve essere successiva alla data di inizio");
        }

        Course saved = courseRepository.save(course);
        return toResponse(saved);
    }

    public void deleteCourse(String codiceCorso) {
        Course course = courseRepository.findByCodiceCorso(codiceCorso)
                .orElseThrow(() -> new ResourceNotFoundException("Corso non trovato con codice: " + codiceCorso));
        courseRepository.delete(course);
    }

    public LessonResponse addLesson(String codiceCorso, LessonRequest request) {
        Course course = courseRepository.findByCodiceCorso(codiceCorso)
                .orElseThrow(() -> new ResourceNotFoundException("Corso non trovato con codice: " + codiceCorso));

        if (lessonRepository.existsByCourseIdAndNumero(course.getId(), request.numero())) {
            throw new DuplicateResourceException("Lezione numero " + request.numero() + " già esistente nel corso");
        }

        Lesson lesson = new Lesson();
        lesson.setCourse(course);
        lesson.setNumero(request.numero());
        lesson.setData(request.data());
        lesson.setOraInizio(request.oraInizio());
        lesson.setDurata(request.durata());
        lesson.setAula(request.aula());
        lesson.setArgomento(request.argomento());

        Lesson saved = lessonRepository.save(lesson);
        
        // AGGIUNTA: Aggiorna la lista delle lezioni in memoria sul corso corrente
        if (course.getLessons() != null) {
            course.getLessons().add(saved);
        }
        
        return toLessonResponse(saved);
    }
    public void addInstrumentToCourse(String codiceCorso, String codiceStrumento) {
        Course course = courseRepository.findByCodiceCorso(codiceCorso)
                .orElseThrow(() -> new ResourceNotFoundException("Corso non trovato con codice: " + codiceCorso));

        Instrument instrument = instrumentRepository.findByCodiceStrumento(codiceStrumento)
                .orElseThrow(() -> new ResourceNotFoundException("Strumento non trovato con codice: " + codiceStrumento));

        if (course.getInstruments().contains(instrument)) {
            throw new DuplicateResourceException("Strumento già associato al corso");
        }

        course.getInstruments().add(instrument);
        courseRepository.save(course);
    }

    private CourseResponse toResponse(Course course) {
        List<LessonResponse> lezioni = course.getLessons().stream()
                .map(this::toLessonResponse)
                .toList();

        return new CourseResponse(
                course.getId(),
                course.getCodiceCorso(),
                course.getNome(),
                course.getDataInizio(),
                course.getDataFine(),
                course.getCostoOrario(),
                course.getTotaleOre(),
                course.getCostoTotale(),
                course.getDurataGiorni(),
                course.isOnline(),
                course.getLivello(),
                course.getTeacher() != null ? course.getTeacher().getNomeCompleto() : null,
                course.getEnrollments().size(),
                lezioni
        );
    }

    private LessonResponse toLessonResponse(Lesson lesson) {
        return new LessonResponse(
                lesson.getId(),
                lesson.getNumero(),
                lesson.getData(),
                lesson.getOraInizio(),
                lesson.getDurata(),
                lesson.getAula(),
                lesson.getArgomento()
        );
    }
}
