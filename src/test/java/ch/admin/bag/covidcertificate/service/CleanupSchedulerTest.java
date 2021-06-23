package ch.admin.bag.covidcertificate.service;

import com.flextrade.jfixture.JFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class CleanupSchedulerTest {
    @InjectMocks
    private CleanupScheduler cleanupScheduler;

    @Mock
    private CertificatePrintService certificatePrintService;

    private final JFixture fixture = new JFixture();

    private static final Integer NUMBER_OF_DAYS_IN_THE_PAST = 10;

    @BeforeEach
    public void init() throws IOException {
        ReflectionTestUtils.setField(cleanupScheduler, "numberOfDaysInThePast", NUMBER_OF_DAYS_IN_THE_PAST);
    }

    @Nested
    class DeleteProcessedCertificatesModifiedUntilDate {
        @Test
        void shouldDeleteAllCertificatesProcessedBeforeConfiguredNumberOfDays() {
            var now = fixture.create(LocalDateTime.class);
            var expected = now.minusDays(NUMBER_OF_DAYS_IN_THE_PAST);

            try (MockedStatic<LocalDateTime> localDateTimeMock = Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS)) {
                localDateTimeMock.when(LocalDateTime::now).thenReturn(now);
                cleanupScheduler.deleteProcessedCertificatesModifiedUntilDate();
                verify(certificatePrintService, times(1)).deleteProcessedCertificatesModifiedUntilDate(expected);
            }
        }
    }
}