package com.scuoladimusica.config;

import com.scuoladimusica.model.entity.ERole;
import com.scuoladimusica.model.entity.Role;
import com.scuoladimusica.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Inizializza i dati essenziali all'avvio dell'applicazione.
 * Crea i ruoli se non esistono già (idempotente).
 */
@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        creaRuoloSeNonEsiste(ERole.ROLE_STUDENT);
        creaRuoloSeNonEsiste(ERole.ROLE_TEACHER);
        creaRuoloSeNonEsiste(ERole.ROLE_ADMIN);
        logger.info("Ruoli inizializzati con successo");
    }

    private void creaRuoloSeNonEsiste(ERole nomeRuolo) {
        if (roleRepository.findByName(nomeRuolo).isEmpty()) {
            roleRepository.save(new Role(nomeRuolo));
            logger.info("Ruolo creato: {}", nomeRuolo);
        }
    }
}
