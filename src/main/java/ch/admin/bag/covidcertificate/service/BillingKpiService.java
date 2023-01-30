package ch.admin.bag.covidcertificate.service;

import ch.admin.bag.covidcertificate.domain.BillingKpiMapper;
import ch.admin.bag.covidcertificate.domain.BillingKpiRepository;
import ch.admin.bag.covidcertificate.domain.CertificatePrintQueueItem;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;

@Service
@Slf4j
@RequiredArgsConstructor
public class BillingKpiService {
    private final BillingKpiRepository billingKpiRepository;
    private final BillingCsvWriterService billingCsvWriterService;

    public void saveKpiOfProcessedCertificates(Collection<CertificatePrintQueueItem> certificatePrintQueueItems) {
        billingKpiRepository.saveAll(BillingKpiMapper.mapAll(certificatePrintQueueItems));
    }

    public FileSystemResource getBillingInformation(
            LocalDate processedAtSince, LocalDate processedAtUntil, String billingFilename)
            throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {

        var until = processedAtUntil == null? LocalDateTime.now() : processedAtUntil.atStartOfDay();
        var billingFile = new File(billingFilename);
        var billingData = billingKpiRepository.getBillingInformation(processedAtSince.atStartOfDay(), until);
        billingCsvWriterService.writeRowsToCsv(billingFile, billingData);
        return new FileSystemResource(billingFile);
    }
}
