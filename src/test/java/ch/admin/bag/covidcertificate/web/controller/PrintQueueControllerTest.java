package ch.admin.bag.covidcertificate.web.controller;

import ch.admin.bag.covidcertificate.api.CertificatePrintQueueItemMapper;
import ch.admin.bag.covidcertificate.api.CertificatePrintRequestDto;
import ch.admin.bag.covidcertificate.domain.CertificatePrintQueueItem;
import ch.admin.bag.covidcertificate.service.CertificatePrintService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.flextrade.jfixture.JFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.handler.ResponseStatusExceptionHandler;

import java.util.stream.Stream;

import static ch.admin.bag.covidcertificate.FixtureCustomization.customizeCertificatePrintRequestDto;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class PrintQueueControllerTest {
    @InjectMocks
    private PrintQueueController controller;
    @Mock
    private CertificatePrintService certificatePrintService;

    private MockMvc mockMvc;

    private final ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().modules(new JavaTimeModule()).build();

    private static final String URL = "/api/v1/print";

    private static final JFixture fixture = new JFixture();

    @BeforeEach
    void setupMocks() {
        customizeCertificatePrintRequestDto(fixture, 100000);
        this.mockMvc = standaloneSetup(controller, new ResponseStatusExceptionHandler()).build();
        lenient().doNothing().when(certificatePrintService).saveCertificateInPrintQueue(any(CertificatePrintQueueItem.class));
    }

    @Nested
    class PrintCertificate {
        @Test
        void savesCertificatePrintRequest() throws Exception {
            var printRequestDto = fixture.create(CertificatePrintRequestDto.class);
            var certificatePrintQueueItem = fixture.create(CertificatePrintQueueItem.class);

            try (MockedStatic<CertificatePrintQueueItemMapper> certificatePrintQueueItemMapperMock = Mockito.mockStatic(CertificatePrintQueueItemMapper.class)) {
                certificatePrintQueueItemMapperMock.when((MockedStatic.Verification) CertificatePrintQueueItemMapper.create(printRequestDto)).thenReturn(certificatePrintQueueItem);

                mockMvc.perform(post(URL)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("Authorization", fixture.create(String.class))
                        .content(mapper.writeValueAsString(printRequestDto)))
                        .andExpect(status().isCreated());

                verify(certificatePrintService).saveCertificateInPrintQueue(certificatePrintQueueItem);
            }
        }

        @Test
        void returnsCreatedStatus() throws Exception {
            var printRequestDto = fixture.create(CertificatePrintRequestDto.class);

            mockMvc.perform(post(URL)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .header("Authorization", fixture.create(String.class))
                    .content(mapper.writeValueAsString(printRequestDto)))
                    .andExpect(status().isCreated());
        }

        @ParameterizedTest
        @MethodSource("ch.admin.bag.covidcertificate.web.controller.PrintQueueControllerTest#invalidCertificatePrintRequestDtos")
        void validatesInputAndReturnsBadRequest_ifInputInvalid(CertificatePrintRequestDto printRequestDto) throws Exception {
            mockMvc.perform(post(URL)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .header("Authorization", fixture.create(String.class))
                    .content(mapper.writeValueAsString(printRequestDto)))
                    .andExpect(status().isBadRequest());
        }
    }

    private static Stream<CertificatePrintRequestDto> invalidCertificatePrintRequestDtos() {
        var printRequestDto1 = fixture.create(CertificatePrintRequestDto.class);
        ReflectionTestUtils.setField(printRequestDto1, "pdfCertificate", null);
        var printRequestDto2 = fixture.create(CertificatePrintRequestDto.class);
        ReflectionTestUtils.setField(printRequestDto2, "uvci", null);
        return Stream.of(printRequestDto1, printRequestDto2);
    }
}
