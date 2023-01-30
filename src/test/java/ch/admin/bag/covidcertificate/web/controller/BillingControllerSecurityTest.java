package ch.admin.bag.covidcertificate.web.controller;

import ch.admin.bag.covidcertificate.config.security.OAuth2SecuredWebConfiguration;
import ch.admin.bag.covidcertificate.service.BillingKpiService;
import com.flextrade.jfixture.JFixture;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = {BillingController.class, OAuth2SecuredWebConfiguration.class},
        properties = {"cc-printing-service.internal.maintenance.user=testUser",
                "cc-printing-service.internal.maintenance.password={noop}secret"
        }
)
@ActiveProfiles("local")
class BillingControllerSecurityTest {
    @MockBean
    private BillingKpiService billingKpiService;

    @Autowired
    private MockMvc mockMvc;

    private static final JFixture fixture = new JFixture();

    private static final String BASE_URL = "/api/int/v1/billing/";
    private static final String VALID_USER = "testUser";
    private static final String VALID_PASSWORD = "secret";


    @BeforeEach
    void setupMocks() throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
        var fileSystemResourceMock = mock(FileSystemResource.class);
        lenient().when(fileSystemResourceMock.contentLength()).thenReturn(fixture.create(Long.class));
        lenient().when(fileSystemResourceMock.getInputStream()).thenReturn(new ByteArrayInputStream(fixture.create(byte[].class)));
        lenient().when(billingKpiService.getBillingInformation(any(), any(), any())).thenReturn(fileSystemResourceMock);
    }

    @Nested
    class GetBillingInformation {
        private final LocalDate sinceDate = fixture.create(LocalDate .class);
        private final String URL = BASE_URL+sinceDate.toString();

        @Test
        void returnsOKIfValidUsernameAndPassword() throws Exception {
            callGetValueSetsWithToken(URL, VALID_USER, VALID_PASSWORD, HttpStatus.OK);
            verify(billingKpiService, times(1)).getBillingInformation(any(), any(), any());
        }


        @ParameterizedTest
        @CsvSource(delimiter = ';', value = {VALID_USER+";invalid","invalid;"+VALID_PASSWORD})
        void returnsUnauthorizedIfUsernameOrPasswordIsInvalid(String userName, String password) throws Exception {
            callGetValueSetsWithToken(URL, userName, password, HttpStatus.UNAUTHORIZED);
            verify(billingKpiService, times(0)).getBillingInformation(any(), any(), any());
        }
    }

    private void callGetValueSetsWithToken(String url, String user, String password, HttpStatus status) throws Exception {
        mockMvc.perform(get(url)
                .with(httpBasic(user, password)))
                .andExpect(getResultMatcher(status));
    }

    private ResultMatcher getResultMatcher(HttpStatus status) {
        return switch (status) {
            case OK -> status().isOk();
            case UNAUTHORIZED -> status().isUnauthorized();
            default -> throw new IllegalArgumentException("HttpStatus not found!");
        };
    }
}
