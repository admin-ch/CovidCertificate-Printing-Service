package ch.admin.bag.covidcertificate.service;

import ch.admin.bag.covidcertificate.domain.CertificatePrintQueueItem;
import ch.admin.bag.covidcertificate.domain.CertificatePrintQueueRepository;
import ch.admin.bag.covidcertificate.domain.CertificatePrintStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class CertificatePrintService {
    private final CertificatePrintQueueRepository certificatePrintQueueRepository;

    @Value("${cc-printing-service.print-queue.max-error-count}")
    private int maxErrorCount;

    public Page<CertificatePrintQueueItem> getNotProcessedItems(LocalDateTime createdBeforeTimestamp, Integer size) {
        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.ASC, "createdAt"));

        return certificatePrintQueueRepository.getNotProcessedItems(createdBeforeTimestamp, pageable);
    }

    public void saveCertificateInPrintQueue(CertificatePrintQueueItem certificatePrintQueueItem){
        certificatePrintQueueRepository.saveAndFlush(certificatePrintQueueItem);
    }

    public void increaseErrorCount(Collection<CertificatePrintQueueItem> certificatePrintQueueItems){
        log.info("Increasing error count for {} certificates", certificatePrintQueueItems.size());
        certificatePrintQueueItems.forEach(this::increaseErrorCount);
        certificatePrintQueueRepository.saveAll(certificatePrintQueueItems);
    }

    private void increaseErrorCount(CertificatePrintQueueItem certificatePrintQueueItem){
        certificatePrintQueueItem.setErrorCount(certificatePrintQueueItem.getErrorCount()+1);
        certificatePrintQueueItem.setModifiedAt(LocalDateTime.now());
        if(Objects.equals(certificatePrintQueueItem.getErrorCount(), maxErrorCount)){
            log.info("Printing certificate {} has failed too many times. Times: {}.", certificatePrintQueueItem.getUvci(), maxErrorCount);
            certificatePrintQueueItem.setStatus(CertificatePrintStatus.ERROR.name());
        }
    }

    public void updateStatus(Collection<CertificatePrintQueueItem> certificatePrintQueueItems, CertificatePrintStatus status){
        log.info("Updating status {} for {} certificates", status.name(), certificatePrintQueueItems.size());
        certificatePrintQueueItems.forEach(it -> {
            it.setStatus(status.name());
            it.setModifiedAt(LocalDateTime.now());
        });
        certificatePrintQueueRepository.saveAll(certificatePrintQueueItems);
    }

    @Transactional
    public void deleteProcessedCertificatesModifiedUntilDate(LocalDateTime dateTime){
        log.info("Deleting certificates processed before {}", dateTime);
        int deletedRowCount = certificatePrintQueueRepository.deleteItemsProcessedBeforeTimestamp(dateTime);
        log.info("Deleted {} certificates", deletedRowCount);
    }


    @Transactional
    public void updateFailedAndResetErrorCount(){
        log.info("Updating failed Certificates and resetting error count");
        int updatedRowCount = certificatePrintQueueRepository.updateFailedAndResetErrorCount();
        log.info("Updated {} failed certificates", updatedRowCount);
    }
}
