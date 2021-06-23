package ch.admin.bag.covidcertificate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = "CF_INSTANCE_INDEX", havingValue = "0")
public class PrintQueueScheduler {
    private final CertificatePrintingJob certificatePrintingJob;

    @Scheduled(cron = "${cc-printing-service.print-queue.schedule}")
    void sendOverSftp() {
        certificatePrintingJob.sendOverSftp();
    }
}
