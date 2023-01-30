package ch.admin.bag.covidcertificate.web.controller;

import ch.admin.bag.covidcertificate.service.BillingKpiService;
import com.flextrade.jfixture.JFixture;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.handler.ResponseStatusExceptionHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class BillingControllerTest {
    @InjectMocks
    private BillingController controller;
    @Mock
    private BillingKpiService billingKpiService;

    private MockMvc mockMvc;

    private static final String BASE_URL = "/api/int/v1/billing";

    private static final JFixture fixture = new JFixture();

    @BeforeEach
    void setupMocks() throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
        this.mockMvc = standaloneSetup(controller, new ResponseStatusExceptionHandler()).build();
        var fileSystemResourceMock = mock(FileSystemResource.class);
        lenient().when(fileSystemResourceMock.contentLength()).thenReturn(fixture.create(Long.class));
        lenient().when(fileSystemResourceMock.getInputStream()).thenReturn(new ByteArrayInputStream(fixture.create(byte[].class)));
        lenient().when(billingKpiService.getBillingInformation(any(), any(), any())).thenReturn(fileSystemResourceMock);
    }

    @Nested
    class GetBillingInformation {
        private final LocalDate sinceDate = fixture.create(LocalDate.class);
        private final String URL = BASE_URL+"/"+sinceDate;

        @Test
        void returnsCreatedStatus() throws Exception {
            mockMvc.perform(get(URL))
                    .andExpect(status().isOk());
        }

        @Test
        void shouldCallBillingKpiServiceToGetAggregatedBillingInformation_withCorrectSinceDate() throws Exception {
            mockMvc.perform(get(URL))
                    .andExpect(status().isOk());

            verify(billingKpiService).getBillingInformation(eq(sinceDate), any(), any());
        }

        @Test
        void shouldCallBillingKpiServiceToGetAggregatedBillingInformation_withCorrectUntilDate() throws Exception {
            var untilDate = fixture.create(LocalDate.class);

            mockMvc.perform(get(URL+"?until="+untilDate.toString()))
                        .andExpect(status().isOk());

            verify(billingKpiService).getBillingInformation(any(), eq(untilDate), any());
        }

        @Test
        void shouldCallBillingKpiServiceToGetAggregatedBillingInformation_withNullUntilDate_ifNoneProvided() throws Exception {
            mockMvc.perform(get(URL))
                    .andExpect(status().isOk());

            verify(billingKpiService).getBillingInformation(any(), isNull(), any());
        }

        @Test
        void shouldCallBillingKpiServiceToGetAggregatedBillingInformation_withCorrectFilename() throws Exception {
            var filename = "billing.csv";
            mockMvc.perform(get(URL))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition", "attachment; filename="+filename));

            verify(billingKpiService).getBillingInformation(any(), any(), eq(filename));
        }

        @Test
        void shouldReturnInBodyTheResultsFromTheBillingKpiService() throws Exception {
            var fileSystemResourceMock = mock(FileSystemResource.class);
            var body = fixture.create(byte[].class);
            when(fileSystemResourceMock.contentLength()).thenReturn(fixture.create(Long.class));
            when(fileSystemResourceMock.getInputStream()).thenReturn(new ByteArrayInputStream(body));
            when(billingKpiService.getBillingInformation(any(), any(), any())).thenReturn(fileSystemResourceMock);

            mockMvc.perform(get(URL))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.parseMediaType("text/csv")))
                    .andExpect(content().bytes(body));
        }


    }
}
