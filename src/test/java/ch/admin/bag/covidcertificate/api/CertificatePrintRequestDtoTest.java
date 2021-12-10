package ch.admin.bag.covidcertificate.api;

import ch.admin.bag.covidcertificate.api.error.InputValidationException;
import com.flextrade.jfixture.JFixture;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static ch.admin.bag.covidcertificate.FixtureCustomization.customizeCertificatePrintRequestDto;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CertificatePrintRequestDtoTest {
    private final JFixture fixture = new JFixture();

    @Nested
    class Validate{
        @Test
        void throwsExceptionIfPDFSizeIsLessThan50KB() {
            var certificatePrintRequestDto = createCertificatePrintRequestDto(49999);

            assertThrows(InputValidationException.class, certificatePrintRequestDto::validate);
        }

        @Test
        void throwsExceptionIfPDFSizeIsMoreThan250KB() {
            var certificatePrintRequestDto = createCertificatePrintRequestDto(250*1024+1);

            assertThrows(InputValidationException.class, certificatePrintRequestDto::validate);
        }

        @ParameterizedTest
        @ValueSource(ints = {50*1024, 100000, 250*1024})
        void doesNotThrowExceptionIfPDFSizeIsBetween50KBand250KB(int pdfSize) {
            var certificatePrintRequestDto = createCertificatePrintRequestDto(pdfSize);

            assertDoesNotThrow(certificatePrintRequestDto::validate);
        }

        private CertificatePrintRequestDto createCertificatePrintRequestDto(int pdfSize){
            customizeCertificatePrintRequestDto(fixture, pdfSize);
            return fixture.create(CertificatePrintRequestDto.class);
        }
    }

}