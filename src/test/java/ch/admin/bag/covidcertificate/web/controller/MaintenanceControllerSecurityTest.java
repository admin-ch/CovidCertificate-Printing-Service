package ch.admin.bag.covidcertificate.web.controller;

import ch.admin.bag.covidcertificate.config.security.OAuth2SecuredWebConfiguration;
import ch.admin.bag.covidcertificate.service.CertificatePrintService;
import ch.admin.bag.covidcertificate.service.CertificatePrintingJob;
import com.flextrade.jfixture.JFixture;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = {MaintenanceController.class, OAuth2SecuredWebConfiguration.class},
        properties = {"cc-printing-service.internal.maintenance.user=testUser",
                "cc-printing-service.internal.maintenance.password={noop}secret"
        }
)
@ActiveProfiles("local")
class MaintenanceControllerSecurityTest {
    @MockBean
    private CertificatePrintService certificatePrintService;
    @MockBean
    private CertificatePrintingJob certificatePrintingJob;

    @Autowired
    private MockMvc mockMvc;

    private static final JFixture fixture = new JFixture();

    private static final String BASE_URL = "/api/int/v1";
    private static final String VALID_USER = "testUser";
    private static final String VALID_PASSWORD = "secret";

    @Nested
    class Print {
        private static final String URL = BASE_URL+"/print";

        @Test
        void returnsOKIfValidUsernameAndPassword() throws Exception {
            callGetValueSetsWithToken(URL, VALID_USER, VALID_PASSWORD, HttpStatus.OK);
            verify(certificatePrintingJob, times(1)).sendOverSftpAsync();
        }

        @ParameterizedTest
        @CsvSource(delimiter = ';', value = {VALID_USER+";invalid","invalid;"+VALID_PASSWORD})
        void returnsUnauthorizedIfUsernameOrPasswordIsInvalid(String userName, String password) throws Exception {
            callGetValueSetsWithToken(URL, userName, password, HttpStatus.UNAUTHORIZED);
            verify(certificatePrintingJob, times(0)).sendOverSftpAsync();
        }
    }

    @Nested
    class DeleteProcessedBeforeDate {
        private final LocalDateTime timestamp = fixture.create(LocalDateTime .class);
        private final String URL = BASE_URL+"/delete-processed/"+timestamp.toString();

        @Test
        void returnsOKIfValidUsernameAndPassword() throws Exception {
            callGetValueSetsWithToken(URL, VALID_USER, VALID_PASSWORD, HttpStatus.OK);
            verify(certificatePrintService, times(1)).deleteProcessedCertificatesModifiedUntilDate(any());
        }

        @ParameterizedTest
        @CsvSource(delimiter = ';', value = {VALID_USER+";invalid","invalid;"+VALID_PASSWORD})
        void returnsUnauthorizedIfUsernameOrPasswordIsInvalid(String userName, String password) throws Exception {
            callGetValueSetsWithToken(URL, userName, password, HttpStatus.UNAUTHORIZED);
            verify(certificatePrintService, times(0)).deleteProcessedCertificatesModifiedUntilDate(any());
        }
    }

    private void callGetValueSetsWithToken(String url, String user, String password, HttpStatus status) throws Exception {
        mockMvc.perform(get(url)
                .with(httpBasic(user, password)))
                .andExpect(getResultMatcher(status));
    }

    private ResultMatcher getResultMatcher(HttpStatus status) {
        switch(status) {
            case OK:
                return status().isOk();
            case UNAUTHORIZED:
                return status().isUnauthorized();
            default:
                throw new IllegalArgumentException("HttpStatus not found!");
        }
    }
}
