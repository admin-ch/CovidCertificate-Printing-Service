package ch.admin.bag.covidcertificate.web.controller;

import ch.admin.bag.covidcertificate.service.BillingKpiService;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/int/v1/billing")
@RequiredArgsConstructor
@Slf4j
public class BillingController {
    private final BillingKpiService billingKpiService;

    @GetMapping(value = "/{processedAtSince}", produces = "text/csv")
    public ResponseEntity getBillingInformation(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate processedAtSince,
            @RequestParam(name = "until", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate processedAtUntil) throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
        var filename = "billing.csv";
        var billingResource = billingKpiService.getBillingInformation(processedAtSince, processedAtUntil, filename);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename="+filename)
                .contentLength(billingResource.contentLength())
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(billingResource);
    }
}
