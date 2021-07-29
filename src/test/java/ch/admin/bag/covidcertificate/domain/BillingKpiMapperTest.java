package ch.admin.bag.covidcertificate.domain;

import com.flextrade.jfixture.JFixture;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BillingKpiMapperTest {
    private final JFixture fixture = new JFixture();

    @Test
    void shouldMapAllCertificates(){
        var certificates = fixture.collections().createCollection(CertificatePrintQueueItem.class);
        var actual = BillingKpiMapper.mapAll(certificates);
        assertEquals(certificates.size(), actual.size());
    }

    @Test
    void shouldMapCantonCodeSender(){
        var certificate = fixture.create(CertificatePrintQueueItem.class);
        var actual = BillingKpiMapper.mapAll(Collections.singletonList(certificate));
        assertEquals(1, actual.size());
        assertEquals(certificate.getCantonCodeSender(), actual.get(0).getCantonCodeSender());
    }
    @Test
    void shouldMapUVCI(){
        var certificate = fixture.create(CertificatePrintQueueItem.class);
        var actual = BillingKpiMapper.mapAll(Collections.singletonList(certificate));
        assertEquals(1, actual.size());
        assertEquals(certificate.getUvci(), actual.get(0).getUvci());
    }
    @Test
    void shouldMapProcessedAt(){
        var certificate = fixture.create(CertificatePrintQueueItem.class);
        var actual = BillingKpiMapper.mapAll(Collections.singletonList(certificate));
        assertEquals(1, actual.size());
        assertEquals(certificate.getModifiedAt(), actual.get(0).getProcessedAt());
    }
}