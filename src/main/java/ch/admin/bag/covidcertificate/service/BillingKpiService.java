package ch.admin.bag.covidcertificate.service;

import ch.admin.bag.covidcertificate.domain.BillingKpiMapper;
import ch.admin.bag.covidcertificate.domain.BillingKpiRepository;
import ch.admin.bag.covidcertificate.domain.CertificatePrintQueueItem;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BillingKpiService {
    private final BillingKpiRepository billingKpiRepository;
    private final BillingCsvWriterService billingCsvWriterService;

    @Value("${cc-printing-service.billing.since-vaccination-date}")
    private @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sinceVaccinationDate;

    public void saveBillableCertificates(Collection<CertificatePrintQueueItem> certificatePrintQueueItems){
        var billableCertificates = certificatePrintQueueItems.stream()
                .filter(this::isBillable)
                .collect(Collectors.toList());
        billingKpiRepository.saveAll(BillingKpiMapper.mapAll(billableCertificates));
    }

    private boolean isBillable(CertificatePrintQueueItem certificatePrintQueueItem){
        return certificatePrintQueueItem.getVaccinationDate() != null &&
                certificatePrintQueueItem.getVaccinationDate().compareTo(sinceVaccinationDate) >= 0;
    }

    public FileSystemResource getBillingInformation(LocalDate processedAtSince, LocalDate processedAtUntil, String billingFilename) throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
        var until = processedAtUntil == null? LocalDateTime.now() : processedAtUntil.atStartOfDay();
        var billingFile = new File(billingFilename);
        var billingData = billingKpiRepository.getBillingInformation(processedAtSince.atStartOfDay(), until);
        billingCsvWriterService.writeRowsToCsv(billingFile, billingData);
        return new FileSystemResource(billingFile);

    }
}
