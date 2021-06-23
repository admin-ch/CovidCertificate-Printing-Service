package ch.admin.bag.covidcertificate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = "CF_INSTANCE_INDEX", havingValue = "0")
public class CleanupScheduler {
    private final CertificatePrintService certificatePrintService;

    @Value("${cc-printing-service.print-queue.cleanup-until-number-of-days}")
    private Integer numberOfDaysInThePast;

    @Scheduled(cron = "${cc-printing-service.print-queue.cleanup-schedule}")
    void deleteProcessedCertificatesModifiedUntilDate() {
        log.info("Starting job to cleanup certificates processed.");
        var untilTimestamp = LocalDateTime.now().minusDays(numberOfDaysInThePast);
        certificatePrintService.deleteProcessedCertificatesModifiedUntilDate(untilTimestamp);
    }
}
