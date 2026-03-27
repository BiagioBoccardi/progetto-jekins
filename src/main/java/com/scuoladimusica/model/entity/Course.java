package com.scuoladimusica.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Il codice corso non può essere vuoto")
    @Column(unique = true, nullable = false)
    private String codiceCorso;

    @NotBlank(message = "Il nome del corso non può essere vuoto")
    @Column(nullable = false)
    private String nome;

    @NotNull(message = "La data di inizio è obbligatoria")
    @Column(nullable = false)
    private LocalDate dataInizio;

    @NotNull(message = "La data di fine è obbligatoria")
    @Column(nullable = false)
    private LocalDate dataFine;

    @NotNull(message = "Il costo orario è obbligatorio")
    @Positive(message = "Il costo orario deve essere positivo")
    @Column(nullable = false)
    private Double costoOrario;

    @NotNull(message = "Il totale ore è obbligatorio")
    @Positive(message = "Il totale ore deve essere positivo")
    @Column(nullable = false)
    private Integer totaleOre;

    @Column(nullable = false)
    @Builder.Default
    private boolean online = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Livello livello = Livello.PRINCIPIANTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Enrollment> enrollments = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Lesson> lessons = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "course_instruments",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "instrument_id"))
    @Builder.Default
    private List<Instrument> instruments = new ArrayList<>();

    /**
     * Calcola il costo totale del corso (costo orario * totale ore).
     */
    public double getCostoTotale() {
        return costoOrario * totaleOre;
    }

    /**
     * Calcola la durata del corso in giorni.
     */
    public long getDurataGiorni() {
        return ChronoUnit.DAYS.between(dataInizio, dataFine);
    }
}
