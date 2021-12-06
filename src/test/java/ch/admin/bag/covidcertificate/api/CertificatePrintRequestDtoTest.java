package ch.admin.bag.covidcertificate.api;

import ch.admin.bag.covidcertificate.api.error.InputValidationException;
import com.flextrade.jfixture.JFixture;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

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
            var certificatePrintRequestDto = createCertificatePrintRequestDto(250001);

            assertThrows(InputValidationException.class, certificatePrintRequestDto::validate);
        }

        @ParameterizedTest
        @ValueSource(ints = {50000, 100000, 250000})
        void doesNotThrowExceptionIfPDFSizeIsBetween50KBand250KB(int pdfSize) {
            var certificatePrintRequestDto = createCertificatePrintRequestDto(pdfSize);

            assertDoesNotThrow(certificatePrintRequestDto::validate);
        }

        private CertificatePrintRequestDto createCertificatePrintRequestDto(int pdfSize){
            var certificatePrintRequestDto = fixture.create(CertificatePrintRequestDto.class);
            var pdfCertificate = ArrayUtils.toPrimitive(fixture.collections().createCollection(byte.class, pdfSize).toArray(new Byte[pdfSize]));
            ReflectionTestUtils.setField(certificatePrintRequestDto, "pdfCertificate", pdfCertificate);
            return certificatePrintRequestDto;
        }
    }

}