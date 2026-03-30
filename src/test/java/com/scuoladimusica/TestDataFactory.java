package com.scuoladimusica;

import java.time.LocalDate;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.scuoladimusica.model.entity.Course;
import com.scuoladimusica.model.entity.Enrollment;
import com.scuoladimusica.model.entity.Instrument;
import com.scuoladimusica.model.entity.Lesson;
import com.scuoladimusica.model.entity.Livello;
import com.scuoladimusica.model.entity.Loan;
import com.scuoladimusica.model.entity.Student;
import com.scuoladimusica.model.entity.Teacher;
import com.scuoladimusica.model.entity.TipoStrumento;
import com.scuoladimusica.repository.CourseRepository;
import com.scuoladimusica.repository.EnrollmentRepository;
import com.scuoladimusica.repository.InstrumentRepository;
import com.scuoladimusica.repository.LessonRepository;
import com.scuoladimusica.repository.LoanRepository;
import com.scuoladimusica.repository.RoleRepository;
import com.scuoladimusica.repository.StudentRepository;
import com.scuoladimusica.repository.TeacherRepository;
import com.scuoladimusica.repository.UserRepository;

/**
 * Factory per creare dati di test riutilizzabili.
 * Ogni metodo crea e salva l'entity nel database tramite il metodo save()
 * fornito da JpaRepository (non richiede metodi custom).
 */
@Component
public class TestDataFactory {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ========== STUDENTI ==========

    public Student creaStudente(String matricola, String cf, String nome, String cognome,
                                LocalDate dataNascita, Livello livello) {
        Student student = Student.builder()
                .matricola(matricola)
                .cf(cf)
                .nome(nome)
                .cognome(cognome)
                .dataNascita(dataNascita)
                .livello(livello)
                .build();
        return studentRepository.save(student);
    }

    public Student creaStudentePredefinito() {
        return creaStudente("M001", "RSSMRA90A01H501Z", "Mario", "Rossi",
                LocalDate.of(1990, 1, 1), Livello.PRINCIPIANTE);
    }

    public Student creaSecondoStudente() {
        return creaStudente("M002", "BNCMRA85B02H501Z", "Maria", "Bianchi",
                LocalDate.of(1985, 2, 2), Livello.INTERMEDIO);
    }

    public Student creaTerzoStudente() {
        return creaStudente("M003", "VRDGPP78C03H501Z", "Giuseppe", "Verdi",
                LocalDate.of(1978, 3, 3), Livello.AVANZATO);
    }

    // ========== INSEGNANTI ==========

    public Teacher creaInsegnante(String matricola, String cf, String nome, String cognome,
                                   LocalDate dataNascita, Double stipendio, String specializzazione,
                                   int anniEsperienza) {
        Teacher teacher = Teacher.builder()
                .matricolaInsegnante(matricola)
                .cf(cf)
                .nome(nome)
                .cognome(cognome)
                .dataNascita(dataNascita)
                .stipendio(stipendio)
                .specializzazione(specializzazione)
                .anniEsperienza(anniEsperienza)
                .build();
        return teacherRepository.save(teacher);
    }

    public Teacher creaInsegnantePredefinito() {
        return creaInsegnante("I001", "VRDLGU80A01H501Z", "Luigi", "Verdi",
                LocalDate.of(1980, 1, 1), 2500.0, "Pianoforte", 15);
    }

    public Teacher creaSecondoInsegnante() {
        return creaInsegnante("I002", "NRANNA85B02H501Z", "Anna", "Neri",
                LocalDate.of(1985, 5, 5), 2200.0, "Violino", 10);
    }

    // ========== CORSI ==========

    public Course creaCorso(String codice, String nome, LocalDate inizio, LocalDate fine,
                             Double costoOrario, Integer totaleOre, boolean online,
                             Livello livello, Teacher teacher) {
        Course course = Course.builder()
                .codiceCorso(codice)
                .nome(nome)
                .dataInizio(inizio)
                .dataFine(fine)
                .costoOrario(costoOrario)
                .totaleOre(totaleOre)
                .online(online)
                .livello(livello)
                .teacher(teacher)
                .build();
        return courseRepository.save(course);
    }

    public Course creaCorsoPredefinito(Teacher teacher) {
        return creaCorso("C001", "Pianoforte Base",
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30),
                25.0, 40, false, Livello.PRINCIPIANTE, teacher);
    }

    public Course creaCorsoOnline(Teacher teacher) {
        return creaCorso("C002", "Chitarra Online",
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 7, 31),
                20.0, 30, true, Livello.INTERMEDIO, teacher);
    }

    public Course creaTerzoCorso(Teacher teacher) {
        return creaCorso("C003", "Violino Avanzato",
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 12, 31),
                35.0, 60, false, Livello.AVANZATO, teacher);
    }

    // ========== STRUMENTI ==========

    public Instrument creaStrumento(String codice, String nome, TipoStrumento tipo,
                                     String marca, Integer anno) {
        Instrument instrument = Instrument.builder()
                .codiceStrumento(codice)
                .nome(nome)
                .tipoStrumento(tipo)
                .marca(marca)
                .annoProduzione(anno)
                .disponibile(true) // <-- MODIFICA APPLICATA
                .build();
        return instrumentRepository.save(instrument);
    }

    public Instrument creaStrumentoPredefinito() {
        return creaStrumento("S001", "Pianoforte a Coda", TipoStrumento.TASTIERA,
                "Yamaha", 2020);
    }

    public Instrument creaSecondoStrumento() {
        return creaStrumento("S002", "Chitarra Classica", TipoStrumento.CORDA,
                "Fender", 2019);
    }

    public Instrument creaTerzoStrumento() {
        return creaStrumento("S003", "Batteria Acustica", TipoStrumento.PERCUSSIONE,
                "Pearl", 2021);
    }

    // ========== ISCRIZIONI ==========

    public Enrollment creaIscrizione(Student student, Course course, int anno) {
        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .annoIscrizione(anno)
                .build();
        Enrollment saved = enrollmentRepository.save(enrollment);
        
        // <-- MODIFICA APPLICATA: Sincronizza la relazione bidirezionale in memoria
        if (student.getEnrollments() == null) {
            student.setEnrollments(new ArrayList<>());
        }
        student.getEnrollments().add(saved);
        
        return saved;
    }

    public Enrollment creaIscrizioneConVoto(Student student, Course course, int anno, int voto) {
        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .annoIscrizione(anno)
                .votoFinale(voto)
                .build();
        Enrollment saved = enrollmentRepository.save(enrollment);
        
        // <-- MODIFICA APPLICATA: Sincronizza la relazione bidirezionale in memoria
        if (student.getEnrollments() == null) {
            student.setEnrollments(new ArrayList<>());
        }
        student.getEnrollments().add(saved);
        
        return saved;
    }

    // ========== PRESTITI ==========

    public Loan creaPrestito(Instrument instrument, Student student, LocalDate inizio) {
        Loan loan = Loan.builder()
                .instrument(instrument)
                .student(student)
                .dataInizio(inizio)
                .build();
        return loanRepository.save(loan);
    }

    public Loan creaPrestitoChiuso(Instrument instrument, Student student,
                                    LocalDate inizio, LocalDate fine) {
        Loan loan = Loan.builder()
                .instrument(instrument)
                .student(student)
                .dataInizio(inizio)
                .dataFine(fine)
                .build();
        return loanRepository.save(loan);
    }

    // ========== LEZIONI ==========

    public Lesson creaLezione(Course course, int numero, LocalDate data,
                               java.time.LocalTime oraInizio, int durata,
                               String aula, String argomento) {
        Lesson lesson = Lesson.builder()
                .course(course)
                .numero(numero)
                .data(data)
                .oraInizio(oraInizio)
                .durata(durata)
                .aula(aula)
                .argomento(argomento)
                .build();
        return lessonRepository.save(lesson);
    }
}