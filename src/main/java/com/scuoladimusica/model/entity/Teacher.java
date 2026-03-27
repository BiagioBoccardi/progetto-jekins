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
@Table(name = "teachers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La matricola insegnante non può essere vuota")
    @Column(unique = true, nullable = false)
    private String matricolaInsegnante;

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

    @NotNull(message = "Lo stipendio è obbligatorio")
    @Positive(message = "Lo stipendio deve essere positivo")
    @Column(nullable = false)
    private Double stipendio;

    @NotBlank(message = "La specializzazione non può essere vuota")
    @Column(nullable = false)
    private String specializzazione;

    @Min(value = 0, message = "Gli anni di esperienza non possono essere negativi")
    @Column(nullable = false)
    @Builder.Default
    private int anniEsperienza = 0;

    @OneToMany(mappedBy = "teacher")
    @Builder.Default
    private List<Course> courses = new ArrayList<>();

    /**
     * Restituisce il nome completo dell'insegnante.
     */
    public String getNomeCompleto() {
        return nome + " " + cognome;
    }
}
