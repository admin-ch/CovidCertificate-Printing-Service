package ch.admin.bag.covidcertificate.web.controller;

import ch.admin.bag.covidcertificate.domain.CertificatePrintStatus;
import ch.admin.bag.covidcertificate.service.CertificatePrintService;
import ch.admin.bag.covidcertificate.service.CertificatePrintingJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/int/v1/")
@RequiredArgsConstructor
@Slf4j
public class MaintenanceController {
    private final CertificatePrintService certificatePrintService;
    private final CertificatePrintingJob certificatePrintingJob;

    @GetMapping("print")
    public ResponseEntity<HttpStatus> print(@RequestParam(required = false) boolean retryFailed) {
        log.info("Starting sending certificates with status {} for printing", CertificatePrintStatus.CREATED.name());
        if(retryFailed){
            certificatePrintService.updateFailedAndResetErrorCount();
        }
        certificatePrintingJob.sendOverSftpAsync();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("delete-processed/{modifiedAt}")
    public ResponseEntity<HttpStatus> deleteProcessedBeforeDate(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime modifiedAt) {
        log.info("Deleting certificates with status {}", CertificatePrintStatus.PROCESSED.name());
        certificatePrintService.deleteProcessedCertificatesModifiedUntilDate(modifiedAt);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
