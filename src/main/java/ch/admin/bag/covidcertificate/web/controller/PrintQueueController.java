package ch.admin.bag.covidcertificate.web.controller;

import ch.admin.bag.covidcertificate.api.CertificatePrintQueueItemMapper;
import ch.admin.bag.covidcertificate.api.CertificatePrintRequestDto;
import ch.admin.bag.covidcertificate.service.CertificatePrintService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/print")
@RequiredArgsConstructor
@Slf4j
public class PrintQueueController {
    private final CertificatePrintService certificatePrintService;

    @PostMapping
    @PreAuthorize("hasRole('bag-cc-certificatecreator')")
    public ResponseEntity<HttpStatus> print(@Valid @RequestBody CertificatePrintRequestDto certificatePrintRequestDto) {
        certificatePrintRequestDto.validate();
        log.info("Adding certificate with uvci {} to the print queue", certificatePrintRequestDto.getUvci());
        certificatePrintService.saveCertificateInPrintQueue(CertificatePrintQueueItemMapper.create(certificatePrintRequestDto));
        log.info("Successfully added Certificate {} for printing", certificatePrintRequestDto.getUvci());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
