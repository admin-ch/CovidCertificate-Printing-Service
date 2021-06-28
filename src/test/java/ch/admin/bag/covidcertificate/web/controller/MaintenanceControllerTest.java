package ch.admin.bag.covidcertificate.web.controller;

import ch.admin.bag.covidcertificate.service.CertificatePrintService;
import ch.admin.bag.covidcertificate.service.CertificatePrintingJob;
import com.flextrade.jfixture.JFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.handler.ResponseStatusExceptionHandler;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class MaintenanceControllerTest {
    @InjectMocks
    private MaintenanceController controller;
    @Mock
    private CertificatePrintService certificatePrintService;
    @Mock
    private CertificatePrintingJob certificatePrintingJob;

    private MockMvc mockMvc;

    private static final String BASE_URL = "/api/int/v1";

    private static final JFixture fixture = new JFixture();

    @BeforeEach
    void setupMocks() {
        this.mockMvc = standaloneSetup(controller, new ResponseStatusExceptionHandler()).build();
    }

    @Nested
    class Print {
        private static final String URL = BASE_URL+"/print";

        @Test
        void returnsCreatedStatus() throws Exception {
            mockMvc.perform(get(URL)).andExpect(status().isOk());
        }

        @ParameterizedTest
        @ValueSource(strings = {URL, URL+"?retryFailed=false", URL+"?retryFailed=true"})
        void savesStartJobToProcessCertificates(String url) throws Exception {
            mockMvc.perform(get(url)).andExpect(status().isOk());

            verify(certificatePrintingJob).sendOverSftpAsync();
        }

        @ParameterizedTest
        @ValueSource(strings = {URL, URL+"?retryFailed=false"})
        void savesNotRetryFailedCertificates_ifFlagIsNotSetOrIsSetToFalse(String url) throws Exception {
            mockMvc.perform(get(url)).andExpect(status().isOk());

            verifyNoInteractions(certificatePrintService);
        }

        @Test
        void savesRetryFailedCertificates_ifFlagIsToTrue() throws Exception {
            mockMvc.perform(get(URL).queryParam("retryFailed", "true")).andExpect(status().isOk());

            verify(certificatePrintService).updateFailedAndResetErrorCount();
        }
    }

    @Nested
    class DeleteProcessedBeforeDate {
        private static final String URL = BASE_URL+"/delete-processed/";

        @Test
        void returnsCreatedStatus() throws Exception {
            var timestamp = fixture.create(LocalDateTime.class);

            mockMvc.perform(get(URL+timestamp.toString())).andExpect(status().isOk());
        }

        @Test
        void shouldDeleteAllCertificatesBeforeSpecifiedDate() throws Exception {
            var timestamp = fixture.create(LocalDateTime.class);

            mockMvc.perform(get(URL+timestamp.toString())).andExpect(status().isOk());

            verify(certificatePrintService).deleteProcessedCertificatesModifiedUntilDate(timestamp.truncatedTo(ChronoUnit.SECONDS));
        }
    }
}
