package ch.admin.bag.covidcertificate.service;

import ch.admin.bag.covidcertificate.config.SftpConfig;
import ch.admin.bag.covidcertificate.domain.CertificatePrintQueueItem;
import ch.admin.bag.covidcertificate.domain.CertificatePrintStatus;
import com.flextrade.jfixture.JFixture;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CertificatePrintingJobTest {
    @InjectMocks
    private CertificatePrintingJob certificatePrintingJob;

    @Mock
    private SftpConfig.PrintingServiceSftpGateway gateway;
    @Mock
    private CertificatePrintService certificatePrintService;
    @Mock
    private BillingKpiService billingKpiService;
    @Mock
    private ZipService zipService;
    @Mock
    private FileService fileService;

    private final JFixture fixture = new JFixture();

    private static final String TEMP_FOLDER = "tmp/test/certificates";
    private static final Integer zipSize = 100;
    
    @BeforeEach
    public void init() throws IOException {
        ReflectionTestUtils.setField(certificatePrintingJob, "tempFolder", TEMP_FOLDER);
        ReflectionTestUtils.setField(certificatePrintingJob, "zipSize", zipSize);
        lenient().when(fileService.createCertificatesRootDirectory(any())).thenReturn(Path.of(TEMP_FOLDER, "certificates_"+ LocalDateTime.now()));
    }

    @Nested
    class SendOverSftp {
        @Test
        void shouldLoadAllPages() {
            var numberOfPages = 10;
            List<Page<CertificatePrintQueueItem>> pages = createPages(numberOfPages);
            var stubbing = when(certificatePrintService.getNotProcessedItems(any(), any()));
            for(Page<CertificatePrintQueueItem> page: pages){
                stubbing = stubbing.thenReturn(page);
            }

            certificatePrintingJob.sendOverSftp();

            verify(certificatePrintService, times(numberOfPages+1)).getNotProcessedItems(any(), any());
        }

        @Test
        void shouldLoadEachPage_withTheConfiguredSize() {
            Page<CertificatePrintQueueItem> emptyPage = new PageImpl<>(Collections.emptyList());
            when(certificatePrintService.getNotProcessedItems(any(), any()))
                    .thenReturn(createPage())
                    .thenReturn(emptyPage);

            certificatePrintingJob.sendOverSftp();

            verify(certificatePrintService, times(2)).getNotProcessedItems(any(), eq(zipSize));
        }


        @Test
        void shouldLoadEachPage_withTheCorrectTimestamp() {
            Page<CertificatePrintQueueItem> emptyPage = new PageImpl<>(Collections.emptyList());
            when(certificatePrintService.getNotProcessedItems(any(), any()))
                    .thenReturn(createPage())
                    .thenReturn(emptyPage);
            var expected = fixture.create(LocalDateTime.class);

            try (MockedStatic<LocalDateTime> localDateTimeMock = Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS)) {
                localDateTimeMock.when(LocalDateTime::now).thenReturn(expected);
                certificatePrintingJob.sendOverSftp();

                verify(certificatePrintService, times(2)).getNotProcessedItems(eq(expected), any());
            }
        }

        @Test
        void shouldProcessAllPages() throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
            var certificatePrintingJobSpy = spy(certificatePrintingJob);
            Page<CertificatePrintQueueItem> page1 = new PageImpl<>(new ArrayList<>(fixture.collections().createCollection(CertificatePrintQueueItem.class)));
            Page<CertificatePrintQueueItem> page2 = new PageImpl<>(new ArrayList<>(fixture.collections().createCollection(CertificatePrintQueueItem.class)));
            Page<CertificatePrintQueueItem> emptyPage = new PageImpl<>(Collections.emptyList());
            when(certificatePrintService.getNotProcessedItems(any(), any()))
                    .thenReturn(page1)
                    .thenReturn(page2)
                    .thenReturn(emptyPage);

            certificatePrintingJobSpy.sendOverSftp();

            verify(certificatePrintingJobSpy).sendOverSftpPage(page1);
            verify(certificatePrintingJobSpy).sendOverSftpPage(page2);
            verify(certificatePrintingJobSpy, times(2)).sendOverSftpPage(any());
        }

        @ParameterizedTest
        @ValueSource(classes = {IOException.class, CsvRequiredFieldEmptyException.class, CsvDataTypeMismatchException.class, RuntimeException.class})
        void shouldIncreaseErrorCount_ifSendingPageForPrintingFails(Class<? extends Exception> exceptionClass) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
            var certificatePrintingJobSpy = spy(certificatePrintingJob);
            Page<CertificatePrintQueueItem> page = new PageImpl<>(new ArrayList<>(fixture.collections().createCollection(CertificatePrintQueueItem.class)));
            Page<CertificatePrintQueueItem> emptyPage = new PageImpl<>(Collections.emptyList());
            when(certificatePrintService.getNotProcessedItems(any(), any()))
                    .thenReturn(page)
                    .thenReturn(emptyPage);
            var expected = fixture.create(exceptionClass);
            doThrow(expected).when(certificatePrintingJobSpy).sendOverSftpPage(any());

            certificatePrintingJobSpy.sendOverSftp();

            verify(certificatePrintService).increaseErrorCount(page.getContent());
        }
    }

    @Nested
    class SendOverSftpPage {
        @Nested
        class SuccessfulExecution {
            @Test
            void shouldCreatedTempDirectory() throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
                certificatePrintingJob.sendOverSftpPage(createPage());
                verify(fileService, times(1)).createCertificatesRootDirectory(TEMP_FOLDER);
            }

            @Test
            void shouldCreatePdfFiles() throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
                var page = createPage();
                var rootPath = Path.of(fixture.create(String.class), fixture.create(String.class));
                when(fileService.createCertificatesRootDirectory(any())).thenReturn(rootPath);

                certificatePrintingJob.sendOverSftpPage(page);

                verify(fileService, times(1)).createPdfFiles(page, rootPath);
            }

            @Test
            void shouldCreateMetaFile() throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
                var successfullyCertificates = createPage().getContent();
                var rootPath = Path.of(fixture.create(String.class), fixture.create(String.class));
                when(fileService.createCertificatesRootDirectory(any())).thenReturn(rootPath);
                when(fileService.createPdfFiles(any(), any())).thenReturn(successfullyCertificates);

                certificatePrintingJob.sendOverSftpPage(createPage());

                verify(fileService, times(1)).createMetaFile(successfullyCertificates, rootPath);
            }

            @Test
            void shouldZipFolder() throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
                var rootPath = Path.of(fixture.create(String.class), fixture.create(String.class));
                File zipFile = rootPath.getParent().resolve(rootPath.toFile().getName() + ".zip").toFile();
                when(fileService.createCertificatesRootDirectory(any())).thenReturn(rootPath);

                certificatePrintingJob.sendOverSftpPage(createPage());

                verify(zipService, times(1)).zipIt(rootPath, zipFile);
            }

            @Test
            void shouldSendZipFileOverSftp() throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
                var rootPath = Path.of(fixture.create(String.class), fixture.create(String.class));
                File zipFile = rootPath.getParent().resolve(rootPath.toFile().getName() + ".zip").toFile();
                when(fileService.createCertificatesRootDirectory(any())).thenReturn(rootPath);

                certificatePrintingJob.sendOverSftpPage(createPage());

                verify(gateway, times(1)).sendToSftp(zipFile);
            }

            @Test
            void shouldUpdateStatusOfSuccessfullyProcessedCertificates() throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
                var successfullyCertificates = createPage().getContent();
                when(fileService.createPdfFiles(any(), any())).thenReturn(successfullyCertificates);

                certificatePrintingJob.sendOverSftpPage(createPage());

                verify(certificatePrintService, times(1)).updateStatus(successfullyCertificates, CertificatePrintStatus.PROCESSED);
            }

            @Test
            void shouldBillSuccessfullyProcessedCertificates() throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
                var successfullyProcessedCertificates = createPage().getContent();
                when(fileService.createPdfFiles(any(), any())).thenReturn(successfullyProcessedCertificates);

                certificatePrintingJob.sendOverSftpPage(createPage());

                verify(billingKpiService, times(1)).saveKpiOfProcessedCertificates(successfullyProcessedCertificates);
            }


            @Test
            void shouldDeleteTempFolder() throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
                var rootPath = Path.of(fixture.create(String.class), fixture.create(String.class));
                File zipFile = rootPath.getParent().resolve(rootPath.toFile().getName() + ".zip").toFile();
                when(fileService.createCertificatesRootDirectory(any())).thenReturn(rootPath);

                certificatePrintingJob.sendOverSftpPage(createPage());

                verify(fileService, times(1)).deleteTempData(rootPath, zipFile);
            }
        }

        @Nested
        class UnSuccessfulExecution {
            @Test
            void shouldTrowException_ifCreateTempDirectoryThrowsException() throws IOException {
                var expected = fixture.create(IOException.class);
                when(fileService.createCertificatesRootDirectory(any())).thenThrow(expected);

                var actual = assertThrows(IOException.class,
                        () -> certificatePrintingJob.sendOverSftpPage(createPage())
                );

                assertEquals(expected, actual);
            }

            @Test
            void shouldNotUpdateCertificates_ifCreateTempDirectoryThrowsException() throws IOException {
                when(fileService.createCertificatesRootDirectory(any())).thenThrow(IOException.class);

                assertThrows(IOException.class,
                        () -> certificatePrintingJob.sendOverSftpPage(createPage())
                );

                verifyNoInteractions(certificatePrintService);
            }

            @Test
            void shouldCreateMetaFile() throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
                var successfullyCertificates = createPage().getContent();
                var rootPath = Path.of(fixture.create(String.class), fixture.create(String.class));
                when(fileService.createCertificatesRootDirectory(any())).thenReturn(rootPath);
                when(fileService.createPdfFiles(any(), any())).thenReturn(successfullyCertificates);

                certificatePrintingJob.sendOverSftpPage(createPage());

                verify(fileService, times(1)).createMetaFile(successfullyCertificates, rootPath);
            }

            @ParameterizedTest
            @ValueSource(classes = {IOException.class, CsvRequiredFieldEmptyException.class, CsvDataTypeMismatchException.class, RuntimeException.class})
            void shouldTrowException_ifCreateMetaFileThrowsException(Class<? extends Exception> exceptionClass) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
                var expected = fixture.create(exceptionClass);
                doThrow(expected).when(fileService).createMetaFile(any(), any());

                var actual = assertThrows(exceptionClass,
                        () -> certificatePrintingJob.sendOverSftpPage(createPage())
                );

                assertEquals(expected, actual);
            }

            @ParameterizedTest
            @ValueSource(classes = {IOException.class, CsvRequiredFieldEmptyException.class, CsvDataTypeMismatchException.class, RuntimeException.class})
            void shouldNotUpdateCertificates_ifCreateMetaFileDirectoryThrowsException(Class<? extends Exception> exceptionClass) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
                doThrow(exceptionClass).when(fileService).createMetaFile(any(), any());

                assertThrows(exceptionClass,
                        () -> certificatePrintingJob.sendOverSftpPage(createPage())
                );

                verifyNoInteractions(certificatePrintService);
            }

            @ParameterizedTest
            @ValueSource(classes = {IOException.class, CsvRequiredFieldEmptyException.class, CsvDataTypeMismatchException.class, RuntimeException.class})
            void shouldDeleteTempFiles_ifCreateMetaFileDirectoryThrowsException(Class<? extends Exception> exceptionClass) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
                var rootPath = Path.of(fixture.create(String.class), fixture.create(String.class));
                File zipFile = rootPath.getParent().resolve(rootPath.toFile().getName() + ".zip").toFile();
                when(fileService.createCertificatesRootDirectory(any())).thenReturn(rootPath);
                doThrow(exceptionClass).when(fileService).createMetaFile(any(), any());

                assertThrows(exceptionClass,
                        () -> certificatePrintingJob.sendOverSftpPage(createPage())
                );

                verify(fileService, times(1)).deleteTempData(rootPath, zipFile);
            }

            @Test
            void shouldTrowException_ifCreateZipFileThrowsException() throws IOException {
                var expected = fixture.create(IOException.class);
                doThrow(expected).when(zipService).zipIt(any(), any());

                var actual = assertThrows(IOException.class,
                        () -> certificatePrintingJob.sendOverSftpPage(createPage())
                );

                assertEquals(expected, actual);
            }

            @Test
            void shouldNotUpdateCertificates_ifCreateZipFileThrowsException() throws IOException {
                doThrow(IOException.class).when(zipService).zipIt(any(), any());

                assertThrows(IOException.class,
                        () -> certificatePrintingJob.sendOverSftpPage(createPage())
                );

                verifyNoInteractions(certificatePrintService);
            }

            @Test
            void shouldDeleteTempFiles_ifCreateZipFileThrowsException() throws IOException {
                var rootPath = Path.of(fixture.create(String.class), fixture.create(String.class));
                File zipFile = rootPath.getParent().resolve(rootPath.toFile().getName() + ".zip").toFile();
                when(fileService.createCertificatesRootDirectory(any())).thenReturn(rootPath);
                doThrow(IOException.class).when(zipService).zipIt(any(), any());

                assertThrows(IOException.class,
                        () -> certificatePrintingJob.sendOverSftpPage(createPage())
                );

                verify(fileService, times(1)).deleteTempData(rootPath, zipFile);
            }

            @Test
            void shouldTrowException_ifSendZipFileOverSftpThrowsException() {
                var page = createPage();
                var expected = fixture.create(RuntimeException.class);
                doThrow(expected).when(gateway).sendToSftp(any());

                var actual = assertThrows(RuntimeException.class,
                        () -> certificatePrintingJob.sendOverSftpPage(page)
                );

                assertEquals(expected, actual);
            }

            @Test
            void shouldNotUpdateCertificates_ifSendZipFileOverSftpThrowsException() {
                var page = createPage();
                doThrow(RuntimeException.class).when(gateway).sendToSftp(any());

                assertThrows(RuntimeException.class,
                        () -> certificatePrintingJob.sendOverSftpPage(page)
                );

                verifyNoInteractions(certificatePrintService);
            }

            @Test
            void shouldDeleteTempFiles_ifSendZipFileOverSftpThrowsException() throws IOException {
                var page = createPage();
                var rootPath = Path.of(fixture.create(String.class), fixture.create(String.class));
                File zipFile = rootPath.getParent().resolve(rootPath.toFile().getName() + ".zip").toFile();
                when(fileService.createCertificatesRootDirectory(any())).thenReturn(rootPath);
                doThrow(RuntimeException.class).when(gateway).sendToSftp(any());

                assertThrows(RuntimeException.class,
                        () -> certificatePrintingJob.sendOverSftpPage(page)
                );

                verify(fileService, times(1)).deleteTempData(rootPath, zipFile);
            }
        }
    }

    private List<Page<CertificatePrintQueueItem>> createPages(int numberOfNonEmptyPages){
        List<Page<CertificatePrintQueueItem>> pages = new ArrayList<>();
        for(int i = 0; i < numberOfNonEmptyPages; i++){
            pages.add(createPage());
        }
        Page<CertificatePrintQueueItem> emptyPage = new PageImpl<>(Collections.emptyList());
        pages.add(emptyPage);
        return pages;
    }

    private Page<CertificatePrintQueueItem> createPage(){
        return new PageImpl<>(new ArrayList<>(fixture.collections().createCollection(CertificatePrintQueueItem.class)));
    }

}