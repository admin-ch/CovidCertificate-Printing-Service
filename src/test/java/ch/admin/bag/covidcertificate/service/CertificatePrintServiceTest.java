package ch.admin.bag.covidcertificate.service;

import ch.admin.bag.covidcertificate.domain.CertificatePrintQueueItem;
import ch.admin.bag.covidcertificate.domain.CertificatePrintQueueRepository;
import ch.admin.bag.covidcertificate.domain.CertificatePrintStatus;
import com.flextrade.jfixture.JFixture;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Array;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CertificatePrintServiceTest {
    @InjectMocks
    private CertificatePrintService certificatePrintService;

    @Mock
    private CertificatePrintQueueRepository certificatePrintQueueRepository;

    private int maxErrorCount = 3;

    private final JFixture fixture = new JFixture();

    @BeforeEach
    void init(){
        ReflectionTestUtils.setField(certificatePrintService, "maxErrorCount", maxErrorCount);
    }

    @Nested
    class GetNotProcessedItem {
        @Test
        void shouldLoadNotProcessItems_withCorrectPageNumber() {
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

            certificatePrintService.getNotProcessedItems(fixture.create(LocalDateTime.class), fixture.create(Integer.class));

            verify(certificatePrintQueueRepository).getNotProcessedItems(any(), pageableCaptor.capture());
            Assertions.assertEquals(0, pageableCaptor.getValue().getPageNumber());
        }

        @Test
        void shouldLoadNotProcessItems_withCorrectPageSize() {
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            int size = fixture.create(Integer.class);

            certificatePrintService.getNotProcessedItems(fixture.create(LocalDateTime.class), size);

            verify(certificatePrintQueueRepository).getNotProcessedItems(any(), pageableCaptor.capture());
            Assertions.assertEquals(size, pageableCaptor.getValue().getPageSize());
        }

        @Test
        void shouldLoadNotProcessItems_withTimestamp() {
            var createdBefore = fixture.create(LocalDateTime.class);

            certificatePrintService.getNotProcessedItems(createdBefore, fixture.create(Integer.class));

            verify(certificatePrintQueueRepository).getNotProcessedItems(eq(createdBefore), any());
        }

        @Test
        void shouldLoadNotProcessItems_withCorrectSorting() {
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

            certificatePrintService.getNotProcessedItems(fixture.create(LocalDateTime.class), fixture.create(Integer.class));

            verify(certificatePrintQueueRepository).getNotProcessedItems(any(), pageableCaptor.capture());
            Assertions.assertEquals(Sort.by(Sort.Direction.ASC, "createdAt"), pageableCaptor.getValue().getSort());
        }

        @Test
        void shouldReturnLoadedPage() {
            Page<CertificatePrintQueueItem> expected = new PageImpl<>(new ArrayList<>(fixture.collections().createCollection(CertificatePrintQueueItem.class)));
            when(certificatePrintQueueRepository.getNotProcessedItems(any(), any())).thenReturn(expected);

            var actual = certificatePrintService.getNotProcessedItems(fixture.create(LocalDateTime.class), fixture.create(Integer.class));

            Assertions.assertEquals(expected, actual);
        }
    }

    @Nested
    class SaveCertificateInPrintQueue {
        @Test
        void shouldSaveCertificate() {
            var certificatePrintQueueItem = fixture.create(CertificatePrintQueueItem.class);
            certificatePrintService.saveCertificateInPrintQueue(certificatePrintQueueItem);
            verify(certificatePrintQueueRepository).saveAndFlush(certificatePrintQueueItem);
        }
    }

    @Nested
    class UpdateStatus {
        @Test
        void shouldSaveAllCertificatesWithCorrectStatus() {
            var certificates = fixture.collections().createCollection(CertificatePrintQueueItem.class);
            certificates.forEach(item -> item.setStatus(CertificatePrintStatus.CREATED.name()));
            var expectedStatus = CertificatePrintStatus.PROCESSED;

            certificatePrintService.updateStatus(certificates, expectedStatus);

            verify(certificatePrintQueueRepository).saveAll(certificates);
            assertTrue(certificates.stream().allMatch(item -> item.getStatus().equals(expectedStatus.name())));
        }

        @Test
        void shouldSaveAllCertificatesWithCurrentDateAsModifiedAt() {
            var certificates = fixture.collections().createCollection(CertificatePrintQueueItem.class);
            var expected = fixture.create(LocalDateTime.class);

            try (MockedStatic<LocalDateTime> localDateTimeMock = Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS)) {
                localDateTimeMock.when(LocalDateTime::now).thenReturn(expected);
                certificatePrintService.updateStatus(certificates, CertificatePrintStatus.PROCESSED);

                verify(certificatePrintQueueRepository).saveAll(certificates);
                assertTrue(certificates.stream().allMatch(item -> item.getModifiedAt().equals(expected)));
            }
        }
    }

    @Nested
    class IncreaseErrorCount {
        @Test
        void shouldSaveAllCertificatesWithIncreasedErrorCount() {
            var errorCount1 = fixture.create(Integer.class);
            var errorCount2 = fixture.create(Integer.class);
            var certificatesErrorCount1 = createCertificatePrintQueueItems(errorCount1);
            var certificatesErrorCount2 = createCertificatePrintQueueItems(errorCount2);
            var certificates = new ArrayList<>(certificatesErrorCount1);
            certificates.addAll(certificatesErrorCount2);

            certificatePrintService.increaseErrorCount(certificates);

            verify(certificatePrintQueueRepository).saveAll(certificates);
            assertTrue(certificatesErrorCount1.stream().allMatch(item -> item.getErrorCount().equals(errorCount1 + 1)));
            assertTrue(certificatesErrorCount2.stream().allMatch(item -> item.getErrorCount().equals(errorCount2 + 1)));
        }

        @Test
        void shouldSaveAllCertificatesWithCorrectStatus() {
            var certificatesZeroErrorCount = createCertificatePrintQueueItems(0);
            var certificatesMaxErrorCount = createCertificatePrintQueueItems(maxErrorCount-1);
            var certificates = new ArrayList<>(certificatesZeroErrorCount);
            certificates.addAll(certificatesMaxErrorCount);

            certificatePrintService.increaseErrorCount(certificates);

            verify(certificatePrintQueueRepository).saveAll(certificates);
            assertTrue(certificatesZeroErrorCount.stream().allMatch(item -> item.getStatus().equals(CertificatePrintStatus.CREATED.name())));
            assertTrue(certificatesMaxErrorCount.stream().allMatch(item -> item.getStatus().equals(CertificatePrintStatus.ERROR.name())));
        }

        @Test
        void shouldSaveAllCertificatesWithCurrentDateAsModifiedAt() {
            var certificates = fixture.collections().createCollection(CertificatePrintQueueItem.class);
            var expected = fixture.create(LocalDateTime.class);

            try (MockedStatic<LocalDateTime> localDateTimeMock = Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS)) {
                localDateTimeMock.when(LocalDateTime::now).thenReturn(expected);
                certificatePrintService.increaseErrorCount(certificates);

                verify(certificatePrintQueueRepository).saveAll(certificates);
                assertTrue(certificates.stream().allMatch(item -> item.getModifiedAt().equals(expected)));
            }
        }

        private List<CertificatePrintQueueItem> createCertificatePrintQueueItems(int errorCount){
            var certificatePrintQueueItems = new ArrayList<>(fixture.collections().createCollection(CertificatePrintQueueItem.class));
            certificatePrintQueueItems.forEach(item -> {
                item.setStatus(CertificatePrintStatus.CREATED.name());
                item.setErrorCount(errorCount);
            });
            return certificatePrintQueueItems;
        }
    }

    @Nested
    class DeleteProcessedCertificatesModifiedUntilDate {
        @Test
        void shouldDeleteAllCertificatesProcessedBeforeGivenDateTime() {
            var expected = fixture.create(LocalDateTime.class);

            certificatePrintService.deleteProcessedCertificatesModifiedUntilDate(expected);

            verify(certificatePrintQueueRepository).deleteItemsProcessedBeforeTimestamp(expected);
        }
    }
}