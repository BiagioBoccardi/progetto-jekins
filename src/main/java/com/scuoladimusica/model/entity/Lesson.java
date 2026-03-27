package com.scuoladimusica.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "lessons",
        uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "numero"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Positive(message = "Il numero della lezione deve essere positivo")
    @Column(nullable = false)
    private int numero;

    @NotNull(message = "La data della lezione è obbligatoria")
    @Column(nullable = false)
    private LocalDate data;

    @NotNull(message = "L'ora di inizio è obbligatoria")
    @Column(nullable = false)
    private LocalTime oraInizio;

    @Positive(message = "La durata deve essere positiva")
    @Column(nullable = false)
    private int durata; // in minuti

    private String aula;
    private String argomento;
}
