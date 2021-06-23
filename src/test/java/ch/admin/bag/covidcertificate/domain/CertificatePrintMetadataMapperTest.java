package ch.admin.bag.covidcertificate.domain;

import com.flextrade.jfixture.JFixture;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(classes = CertificatePrintMetadataMapper.class )
class CertificatePrintMetadataMapperTest {
    @Autowired
    private CertificatePrintMetadataMapper certificatePrintMetadataMapper;

    private final JFixture fixture = new JFixture();

    @Nested
    class MapAll{
        @Test
        void shouldMapAllEntries() {
            var input = fixture.collections().createCollection(CertificatePrintQueueItem.class);
            var actual = certificatePrintMetadataMapper.mapAll(input);
            assertEquals(input.size(), actual.size());
        }

        @Test
        void shouldMapUvci() {
            var input = fixture.create(CertificatePrintQueueItem.class);
            var actual = certificatePrintMetadataMapper.mapAll(Collections.singleton(input));

            assertEquals(input.getUvci(), actual.get(0).getUvci());
        }

        @Test
        void shouldMapAddressLine1() {
            var input = fixture.create(CertificatePrintQueueItem.class);
            var actual = certificatePrintMetadataMapper.mapAll(Collections.singleton(input));

            assertEquals(input.getAddressLine1(), actual.get(0).getAddressLine1());
        }

        @Test
        void shouldMapAddressLine2() {
            var input = fixture.create(CertificatePrintQueueItem.class);
            var actual = certificatePrintMetadataMapper.mapAll(Collections.singleton(input));

            assertEquals(input.getAddressLine2(), actual.get(0).getAddressLine2());
        }

        @Test
        void shouldMapAddressLine3ToNull() {
            var input = fixture.create(CertificatePrintQueueItem.class);
            var actual = certificatePrintMetadataMapper.mapAll(Collections.singleton(input));

            assertNull(actual.get(0).getAddressLine3());
        }

        @Test
        void shouldMapAddressLine4ToNull() {
            var input = fixture.create(CertificatePrintQueueItem.class);
            var actual = certificatePrintMetadataMapper.mapAll(Collections.singleton(input));

            assertNull(actual.get(0).getAddressLine4());
        }

        @Test
        void shouldMapZipCode() {
            var input = fixture.create(CertificatePrintQueueItem.class);
            var actual = certificatePrintMetadataMapper.mapAll(Collections.singleton(input));

            assertEquals(String.valueOf(input.getZipCode()), actual.get(0).getZipCode());
        }

        @Test
        void shouldMapCity() {
            var input = fixture.create(CertificatePrintQueueItem.class);
            var actual = certificatePrintMetadataMapper.mapAll(Collections.singleton(input));

            assertEquals(input.getCity(), actual.get(0).getCity());
        }

        @Test
        void shouldMapSender() {
            var input = fixture.create(CertificatePrintQueueItem.class);
            ReflectionTestUtils.setField(input, "cantonCodeSender", "TE");
            var actual = certificatePrintMetadataMapper.mapAll(Collections.singleton(input));

            assertEquals("1234 test", actual.get(0).getSender());
        }

        @Test
        void shouldMapLanguage() {
            var input = fixture.create(CertificatePrintQueueItem.class);
            var actual = certificatePrintMetadataMapper.mapAll(Collections.singleton(input));

            assertEquals(input.getLanguage(), actual.get(0).getLanguage());
        }

        @Test
        void shouldMapPrio() {
            var input = fixture.create(CertificatePrintQueueItem.class);
            var actual = certificatePrintMetadataMapper.mapAll(Collections.singleton(input));

            assertEquals("1", actual.get(0).getPrio());
        }

        @Test
        void shouldMapFilename() {
            var input = fixture.create(CertificatePrintQueueItem.class);
            var actual = certificatePrintMetadataMapper.mapAll(Collections.singleton(input));

            assertEquals(UvciUtils.mapFilenameFromUVCI(input.getUvci()), actual.get(0).getFilename());
        }
    }

}