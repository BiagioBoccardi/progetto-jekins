package com.scuoladimusica.controller;

import com.scuoladimusica.model.dto.request.InstrumentRequest;
import com.scuoladimusica.model.dto.request.LoanRequest;
import com.scuoladimusica.model.dto.request.ReturnRequest;
import com.scuoladimusica.model.dto.response.InstrumentResponse;
import com.scuoladimusica.model.dto.response.LoanResponse;
import com.scuoladimusica.service.InstrumentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instruments")
public class InstrumentController {

    @Autowired
    private InstrumentService instrumentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InstrumentResponse> createInstrument(
            @Valid @RequestBody InstrumentRequest request) {
        InstrumentResponse response = instrumentService.createInstrument(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<InstrumentResponse>> getAllInstruments() {
        return ResponseEntity.ok(instrumentService.getAllInstruments());
    }

    @GetMapping("/available")
    public ResponseEntity<List<InstrumentResponse>> getAvailableInstruments() {
        return ResponseEntity.ok(instrumentService.getAvailableInstruments());
    }

    @GetMapping("/{codiceStrumento}")
    public ResponseEntity<InstrumentResponse> getInstrument(@PathVariable String codiceStrumento) {
        return ResponseEntity.ok(instrumentService.getInstrumentByCode(codiceStrumento));
    }

    @PostMapping("/{codiceStrumento}/loan")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<LoanResponse> loanInstrument(
            @PathVariable String codiceStrumento,
            @Valid @RequestBody LoanRequest request) {
        LoanResponse response = instrumentService.loanToStudent(
                codiceStrumento, request.matricolaStudente(), request.dataInizio());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{codiceStrumento}/return")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<LoanResponse> returnInstrument(
            @PathVariable String codiceStrumento,
            @Valid @RequestBody ReturnRequest request) {
        return ResponseEntity.ok(instrumentService.returnInstrument(codiceStrumento, request.dataFine()));
    }
}
