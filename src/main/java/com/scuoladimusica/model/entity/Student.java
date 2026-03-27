package com.scuoladimusica.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La matricola non può essere vuota")
    @Column(unique = true, nullable = false)
    private String matricola;

    @NotBlank(message = "Il codice fiscale non può essere vuoto")
    @Size(min = 16, max = 16, message = "Il codice fiscale deve essere di 16 caratteri")
    @Column(nullable = false)
    private String cf;

    @NotBlank(message = "Il nome non può essere vuoto")
    @Column(nullable = false)
    private String nome;

    @NotBlank(message = "Il cognome non può essere vuoto")
    @Column(nullable = false)
    private String cognome;

    @NotNull(message = "La data di nascita è obbligatoria")
    @Past(message = "La data di nascita deve essere nel passato")
    @Column(nullable = false)
    private LocalDate dataNascita;

    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Livello livello = Livello.PRINCIPIANTE;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Enrollment> enrollments = new ArrayList<>();

    /**
     * Restituisce il nome completo dello studente.
     */
    public String getNomeCompleto() {
        return nome + " " + cognome;
    }

    /**
     * Restituisce il numero di corsi a cui lo studente è iscritto.
     */
    public int getNumeroCorsiFrequentati() {
        return enrollments.size();
    }

    /**
     * Calcola la media dei voti registrati (esclude le iscrizioni senza voto).
     * Restituisce 0 se non ci sono voti.
     */
    public double getMediaVoti() {
        List<Integer> voti = enrollments.stream()
                .map(Enrollment::getVotoFinale)
                .filter(v -> v != null)
                .toList();

        if (voti.isEmpty()) return 0.0;

        return voti.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
    }
}
