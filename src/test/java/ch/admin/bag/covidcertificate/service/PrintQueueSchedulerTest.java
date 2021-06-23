package ch.admin.bag.covidcertificate.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PrintQueueSchedulerTest {
    @InjectMocks
    private PrintQueueScheduler printQueueScheduler;

    @Mock
    private CertificatePrintingJob certificatePrintingJob;

    @Test
    void shouldSendCertificatesOverSftp(){
        printQueueScheduler.sendOverSftp();
        verify(certificatePrintingJob).sendOverSftp();
    }


}