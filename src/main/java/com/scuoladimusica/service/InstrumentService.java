package com.scuoladimusica.service;

import com.scuoladimusica.exception.BusinessRuleException;
import com.scuoladimusica.exception.DuplicateResourceException;
import com.scuoladimusica.exception.ResourceNotFoundException;
import com.scuoladimusica.model.dto.request.InstrumentRequest;
import com.scuoladimusica.model.dto.response.InstrumentResponse;
import com.scuoladimusica.model.dto.response.LoanResponse;
import com.scuoladimusica.model.entity.Instrument;
import com.scuoladimusica.model.entity.Loan;
import com.scuoladimusica.model.entity.Student;
import com.scuoladimusica.repository.InstrumentRepository;
import com.scuoladimusica.repository.LoanRepository;
import com.scuoladimusica.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class InstrumentService {

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private StudentRepository studentRepository;

    public InstrumentResponse createInstrument(InstrumentRequest request) {
        if (instrumentRepository.existsByCodiceStrumento(request.codiceStrumento())) {
            throw new DuplicateResourceException("Codice strumento già esistente: " + request.codiceStrumento());
        }

        Instrument instrument = new Instrument();
        instrument.setCodiceStrumento(request.codiceStrumento());
        instrument.setNome(request.nome());
        instrument.setTipoStrumento(request.tipoStrumento());
        instrument.setMarca(request.marca());
        instrument.setAnnoProduzione(request.annoProduzione());
        
        // Se questi campi non esistono nell'Entity Instrument, commentali o rimuovili
        // instrument.setNumeroCorde(request.numeroCorde());
        // instrument.setTipoCorde(request.tipoCorde());
        // instrument.setMateriale(request.materiale());
        // instrument.setTipoPelle(request.tipoPelle());
        // instrument.setDiametro(request.diametro());
        
        instrument.setDisponibile(true); 

        Instrument saved = instrumentRepository.save(instrument);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public InstrumentResponse getInstrumentByCode(String codiceStrumento) {
        Instrument instrument = instrumentRepository.findByCodiceStrumento(codiceStrumento)
                .orElseThrow(() -> new ResourceNotFoundException("Strumento non trovato con codice: " + codiceStrumento));
        return toResponse(instrument);
    }

    @Transactional(readOnly = true)
    public List<InstrumentResponse> getAllInstruments() {
        return instrumentRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InstrumentResponse> getAvailableInstruments() {
        return instrumentRepository.findAvailable()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public LoanResponse loanToStudent(String codiceStrumento, String matricolaStudente, LocalDate dataInizio) {
        Instrument instrument = instrumentRepository.findByCodiceStrumento(codiceStrumento)
                .orElseThrow(() -> new ResourceNotFoundException("Strumento non trovato con codice: " + codiceStrumento));

        Student student = studentRepository.findByMatricola(matricolaStudente)
                .orElseThrow(() -> new ResourceNotFoundException("Studente non trovato con matricola: " + matricolaStudente));

        if (loanRepository.existsByInstrumentIdAndDataFineIsNull(instrument.getId())) {
            throw new BusinessRuleException("Lo strumento è già in prestito");
        }

        Loan loan = new Loan();
        loan.setInstrument(instrument);
        loan.setStudent(student);
        loan.setDataInizio(dataInizio);
        loan.setDataFine(null);

        instrument.setDisponibile(false);
        instrumentRepository.save(instrument);

        Loan saved = loanRepository.save(loan);
        return toLoanResponse(saved);
    }

    public LoanResponse returnInstrument(String codiceStrumento, LocalDate dataFine) {
        Instrument instrument = instrumentRepository.findByCodiceStrumento(codiceStrumento)
                .orElseThrow(() -> new ResourceNotFoundException("Strumento non trovato con codice: " + codiceStrumento));

        Loan loan = loanRepository.findByInstrumentIdAndDataFineIsNull(instrument.getId())
                .orElseThrow(() -> new BusinessRuleException("Nessun prestito attivo per questo strumento"));

        if (dataFine.isBefore(loan.getDataInizio())) {
            throw new BusinessRuleException("La data di restituzione non può essere precedente alla data di inizio prestito");
        }

        loan.setDataFine(dataFine);

        instrument.setDisponibile(true);
        instrumentRepository.save(instrument);

        Loan saved = loanRepository.save(loan);
        return toLoanResponse(saved);
    }

    private InstrumentResponse toResponse(Instrument instrument) {
        return new InstrumentResponse(
                instrument.getId(),
                instrument.getCodiceStrumento(),
                instrument.getNome(),
                instrument.getTipoStrumento(),
                instrument.getMarca(),
                instrument.getAnnoProduzione(),
                instrument.isDisponibile()
        );
    }

    private LoanResponse toLoanResponse(Loan loan) {
        return new LoanResponse(
                loan.getId(),
                loan.getInstrument().getCodiceStrumento(),
                loan.getInstrument().getNome(),
                loan.getStudent().getMatricola(),
                loan.getStudent().getNomeCompleto(),
                loan.getDataInizio(),
                loan.getDataFine()
        );
    }
}