package ch.admin.bag.covidcertificate.service;

import ch.admin.bag.covidcertificate.config.SftpConfig;
import ch.admin.bag.covidcertificate.domain.CertificatePrintQueueItem;
import ch.admin.bag.covidcertificate.domain.CertificatePrintStatus;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CertificatePrintingJob {
    private final SftpConfig.PrintingServiceSftpGateway gateway;
    private final CertificatePrintService certificatePrintService;
    private final ZipService zipService;
    private final FileService fileService;

    @Value("${cc-printing-service.temp-folder}")
    private String tempFolder;

    @Value("${cc-printing-service.zip-size}")
    private Integer zipSize;

    @Async
    public void sendOverSftpAsync(){
        sendOverSftp();
    }

    public void sendOverSftp() {
        log.info("Starting job to send certificates for printing");
        var createdBeforeTimestamp = LocalDateTime.now();
        Page<CertificatePrintQueueItem> certificatePrintQueues = certificatePrintService.getNotProcessedItems(createdBeforeTimestamp, zipSize);
        while(!certificatePrintQueues.getContent().isEmpty())
        {
            try {
                var successfullySentCertificates = sendOverSftpPage(certificatePrintQueues);
                log.info("Successfully sent {} certificates for printing", successfullySentCertificates.size());
            } catch (CsvRequiredFieldEmptyException| CsvDataTypeMismatchException| IOException| RuntimeException e) {
                log.error("Failed to send certificates for printing", e);
                certificatePrintService.increaseErrorCount(certificatePrintQueues.getContent());
            }
            certificatePrintQueues = certificatePrintService.getNotProcessedItems(createdBeforeTimestamp, zipSize);
        }

        log.info("End job to send certificates for printing");
    }

    public List<CertificatePrintQueueItem> sendOverSftpPage(Page<CertificatePrintQueueItem> certificatePrintQueues) throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
        if(certificatePrintQueues.isEmpty()){
            return Collections.emptyList();
        }
        log.info("Preparing {} certificates to send for printing", certificatePrintQueues.getSize());

        var rootPath = fileService.createCertificatesRootDirectory(tempFolder);
        var zipFile = rootPath.getParent().resolve(rootPath.toFile().getName() + ".zip").toFile();
        try {
            var successfullyCreatedCertificates = fileService.createPdfFiles(certificatePrintQueues, rootPath);
            fileService.createMetaFile(successfullyCreatedCertificates, rootPath);
            zipService.zipIt(rootPath, zipFile);
            log.info("Sending {} for printing", zipFile.getName());
            gateway.sendToSftp(zipFile);
            log.info("Successfully sent {} for printing", zipFile.getName());
            certificatePrintService.updateStatus(successfullyCreatedCertificates, CertificatePrintStatus.PROCESSED);
            return successfullyCreatedCertificates;
        }finally {
            fileService.deleteTempData(rootPath, zipFile);
        }
    }
}
