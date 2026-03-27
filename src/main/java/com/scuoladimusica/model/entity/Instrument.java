package com.scuoladimusica.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Instrument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String codiceStrumento;

    private String nome;

    @Enumerated(EnumType.STRING)
    private TipoStrumento tipoStrumento;

    private String marca;

    private Integer annoProduzione;
    
    // Campi opzionali usati nel service
    private Integer numeroCorde;
    private String tipoCorde;
    private String materiale;
    private String tipoPelle;
    private Double diametro;

    private boolean disponibile; 
    
    // Metodi espliciti per sicurezza se Lombok dovesse fare i capricci
    public boolean isDisponibile() {
        return disponibile;
    }

    public void setDisponibile(boolean disponibile) {
        this.disponibile = disponibile;
    }
}