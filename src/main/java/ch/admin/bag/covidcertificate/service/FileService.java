package ch.admin.bag.covidcertificate.service;

import ch.admin.bag.covidcertificate.domain.CertificatePrintMetadataMapper;
import ch.admin.bag.covidcertificate.domain.CertificatePrintQueueItem;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static ch.admin.bag.covidcertificate.domain.UvciUtils.mapFilenameFromUVCI;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileService {
    private final CsvWriterService csvWriterService;
    private final CertificatePrintMetadataMapper certificatePrintMetadataMapper;

    Path createCertificatesRootDirectory(String tempFolder) throws IOException {
        var rootPath = Path.of(tempFolder, "certificates_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS")));
        return createdDirectory(rootPath);
    }

    private Path createdDirectory(Path rootPath) throws IOException {
        try {
            log.info("Creating temp folder {}", rootPath.getFileName());
            rootPath = Files.createDirectories(rootPath).toAbsolutePath();
            log.info("Created temp folder {}", rootPath.getFileName());
        } catch (IOException e) {
            log.error("Failed to created temp folder {}", rootPath.getFileName(), e);
            throw e;
        }
        return rootPath;
    }

    List<CertificatePrintQueueItem> createPdfFiles(Page<CertificatePrintQueueItem> certificatePrintQueues, Path rootPath){
        log.info("Creating pdf files in temp folder : {}", rootPath.getFileName());
        List<CertificatePrintQueueItem> failedCertificates = new ArrayList<>();
        for (CertificatePrintQueueItem item : certificatePrintQueues) {
            var filename = mapFilenameFromUVCI(item.getUvci());
            var file = rootPath.resolve(filename).toFile();
            try {
                createPdf(item, file);
            } catch (IOException e) {
                log.error("Failed to create pdf file for uvci {}", filename, e);
                failedCertificates.add(item);
            }
        }
        log.info("{} pdf files were created", certificatePrintQueues.getSize());

        return certificatePrintQueues.filter(
                certificatePrintQueueItem -> !failedCertificates.contains(certificatePrintQueueItem)
        ).toList();
    }
    void createPdf(CertificatePrintQueueItem printCertificateRequestDto, File file) throws IOException {
        try (OutputStream out = new FileOutputStream(file)) {
            out.write(printCertificateRequestDto.getCertificatePdfData().getPdf());
        }
    }

    void createMetaFile(List<CertificatePrintQueueItem> certificatePrintQueues, Path rootPath) throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
        log.info("Creating metadata csv file");
        var metaFile = rootPath.resolve(rootPath.getFileName()+".csv").toFile();
        try {
            csvWriterService.writeRowsToCsv(metaFile, certificatePrintMetadataMapper.mapAll(certificatePrintQueues));
        } catch (IOException| CsvRequiredFieldEmptyException |CsvDataTypeMismatchException| NullPointerException e) {
            log.error("Failed to created meta file {}", metaFile.getName(), e);
            throw e;
        }
        log.info("Successfully created metadata csv file");
    }



    void deleteTempData(Path directory, File zipFile) throws IOException {
        log.info("Deleting temp folder {} and file {}", directory.getFileName(), zipFile.getName());
        try {
            if(directory.toFile().exists()) {
                deleteDirectory(directory);
            }
        } catch (IOException e) {
            log.error("Failed to delete temp folder{}", directory.getFileName(), e);
            throw e;
        }
        try {
            if(zipFile.exists()) {
                Files.delete(zipFile.toPath());
            }
        } catch (IOException e) {
            log.error("Failed to delete file {}", zipFile.getName(), e);
            throw e;
        }
        log.info("Successfully deleted temp folder {} and file {}", directory.getFileName(), zipFile.getName());
    }

    private void deleteDirectory(Path directoryToBeDeleted) throws IOException {
        File[] allContents = directoryToBeDeleted.toFile().listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file.toPath());
            }
        }
        Files.delete(directoryToBeDeleted);
    }

}
