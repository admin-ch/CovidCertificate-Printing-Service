package ch.admin.bag.covidcertificate.service;

import ch.admin.bag.covidcertificate.domain.*;
import com.flextrade.jfixture.JFixture;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillingKpiServiceTest {
    @InjectMocks
    private BillingKpiService billingKpiService;
    @Mock
    private BillingKpiRepository billingKpiRepository;
    @Mock
    private BillingCsvWriterService billingCsvWriterService;

    private final JFixture fixture = new JFixture();

    @Nested
    class SaveBillableCertificates{
        @Test
        void shouldOnlyMapBillableCertificatesToBillingKpis(){
            var billableCertificates = fixture.collections().createCollection(CertificatePrintQueueItem.class);
            billableCertificates.forEach(certificate -> ReflectionTestUtils.setField(certificate, "isBillable", true));
            var nonBillableCertificates = fixture.collections().createCollection(CertificatePrintQueueItem.class);
            nonBillableCertificates.forEach(certificate -> ReflectionTestUtils.setField(certificate, "isBillable", false));
            var certificates = new ArrayList<>(billableCertificates);
            certificates.addAll(nonBillableCertificates);

            try (MockedStatic<BillingKpiMapper> billingKpiMapperMock = Mockito.mockStatic(BillingKpiMapper.class)) {
                billingKpiMapperMock.when(() -> BillingKpiMapper.mapAll(any())).thenReturn(fixture.collections().createCollection(BillingKpi.class));

                billingKpiService.saveBillableCertificates(certificates);

                // ToDo does no longer compile, find another solution
                //billingKpiMapperMock.verify(times(1), () -> BillingKpiMapper.mapAll(any()));
                billingKpiMapperMock.verify(() -> BillingKpiMapper.mapAll(billableCertificates));
            }
        }

        @Test
        void shouldSaveMappedBillingKpis(){
            var certificates = fixture.collections().createCollection(CertificatePrintQueueItem.class);
            certificates.forEach(certificate -> ReflectionTestUtils.setField(certificate, "isBillable", true));
            var billingKpiList = fixture.collections().createCollection(BillingKpi.class);

            try (MockedStatic<BillingKpiMapper> billingKpiMapperMock = Mockito.mockStatic(BillingKpiMapper.class)) {
                billingKpiMapperMock.when(() -> BillingKpiMapper.mapAll(any())).thenReturn(billingKpiList);

                billingKpiService.saveBillableCertificates(certificates);


                verify(billingKpiRepository).saveAll(billingKpiList);
            }
        }
    }

    @Nested
    class GetBillingInformation {
        @Test
        void shouldCallRepositoryWithProvidedSinceDateAtStartOfTheDay() throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
            var sinceDate = fixture.create(LocalDate.class);
            billingKpiService.getBillingInformation(sinceDate, fixture.create(LocalDate.class), fixture.create(String.class));
            verify(billingKpiRepository).getBillingInformation(eq(sinceDate.atStartOfDay()), any());
        }

        @Test
        void shouldCallRepositoryWithProvidedUntilDateAtStartOfTheDay_ifNotNull() throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
            var untilDate = fixture.create(LocalDate.class);
            billingKpiService.getBillingInformation(fixture.create(LocalDate.class), untilDate, fixture.create(String.class));
            verify(billingKpiRepository).getBillingInformation(any(), eq(untilDate.atStartOfDay()));
        }

        @Test
        void shouldCallRepositoryWithCurrentDateAsUntilDate_ifNotNull() throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
            var now = fixture.create(LocalDateTime.class);

            try (MockedStatic<LocalDateTime> localDateTimeMock = Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS)) {
                localDateTimeMock.when(LocalDateTime::now).thenReturn(now);

                billingKpiService.getBillingInformation(fixture.create(LocalDate.class), null, fixture.create(String.class));

                verify(billingKpiRepository).getBillingInformation(any(), eq(now));
            }
        }

        @Test
        void shouldWriteAggregatedBillingKPIsInCSV() throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
            var aggregatedBillingKpis = new ArrayList<>(fixture.collections().createCollection(AggregatedBillingKpi.class));
            when(billingKpiRepository.getBillingInformation(any(), any())).thenReturn(aggregatedBillingKpis);
            billingKpiService.getBillingInformation(fixture.create(LocalDate.class), fixture.create(LocalDate.class), fixture.create(String.class));
            verify(billingCsvWriterService).writeRowsToCsv(any(), eq(aggregatedBillingKpis));
        }


        @Test
        void shouldWriteAggregatedBillingKPIsInProvidedCSVFile() throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
            var fileName = fixture.create(String.class);
            var aggregatedBillingKpis = new ArrayList<>(fixture.collections().createCollection(AggregatedBillingKpi.class));
            when(billingKpiRepository.getBillingInformation(any(), any())).thenReturn(aggregatedBillingKpis);
            billingKpiService.getBillingInformation(fixture.create(LocalDate.class), fixture.create(LocalDate.class), fileName);
            verify(billingCsvWriterService).writeRowsToCsv(argThat(file -> file.getName().equals(fileName)), eq(aggregatedBillingKpis));
        }

        @Test
        void shouldReturnFileResourceForProvidedCSVFile() throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
            var fileName = fixture.create(String.class);
            var actual = billingKpiService.getBillingInformation(fixture.create(LocalDate.class), fixture.create(LocalDate.class), fileName);
            assertEquals(fileName, actual.getFile().getName());
        }
    }

}