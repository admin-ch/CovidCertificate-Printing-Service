package ch.admin.bag.covidcertificate;

import ch.admin.bag.covidcertificate.api.CertificatePrintRequestDto;
import com.flextrade.jfixture.JFixture;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.test.util.ReflectionTestUtils;

public class FixtureCustomization {
    public static void customizeCertificatePrintRequestDto(JFixture fixture, final int pdfSize) {
        fixture.customise().lazyInstance(CertificatePrintRequestDto.class, () -> {
            var helperFixture = new JFixture();
            var certificatePrintRequestDto = helperFixture.create(CertificatePrintRequestDto.class);
            var pdfCertificate = ArrayUtils.toPrimitive(helperFixture.collections().createCollection(byte.class, pdfSize).toArray(new Byte[pdfSize]));
            ReflectionTestUtils.setField(certificatePrintRequestDto, "pdfCertificate", pdfCertificate);
            return certificatePrintRequestDto;
        });
    }
}
