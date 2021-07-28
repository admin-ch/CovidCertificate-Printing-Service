package ch.admin.bag.covidcertificate.api;

import ch.admin.bag.covidcertificate.domain.CertificatePrintStatus;
import com.flextrade.jfixture.JFixture;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CertificatePrintQueueItemMapperTest {

    private final JFixture fixture = new JFixture();

    @Test
    void shouldMapUVCI(){
        var requestDto = fixture.create(CertificatePrintRequestDto.class);
        var actual = CertificatePrintQueueItemMapper.create(requestDto);
        assertEquals(requestDto.getUvci(), actual.getUvci());
    }

    @Test
    void shouldMapStatus(){
        var requestDto = fixture.create(CertificatePrintRequestDto.class);
        var actual = CertificatePrintQueueItemMapper.create(requestDto);
        assertEquals(CertificatePrintStatus.CREATED.name(), actual.getStatus());
    }

    @Test
    void shouldMapAddressLine1(){
        var requestDto = fixture.create(CertificatePrintRequestDto.class);
        var actual = CertificatePrintQueueItemMapper.create(requestDto);
        assertEquals(requestDto.getAddressLine1(), actual.getAddressLine1());
    }
    @Test
    void shouldMapAddressLine2(){
        var requestDto = fixture.create(CertificatePrintRequestDto.class);
        var actual = CertificatePrintQueueItemMapper.create(requestDto);
        assertEquals(requestDto.getAddressLine2(), actual.getAddressLine2());
    }
    @Test
    void shouldMapZipCode(){
        var requestDto = fixture.create(CertificatePrintRequestDto.class);
        var actual = CertificatePrintQueueItemMapper.create(requestDto);
        assertEquals(requestDto.getZipCode(), actual.getZipCode());
    }
    @Test
    void shouldMapCity(){
        var requestDto = fixture.create(CertificatePrintRequestDto.class);
        var actual = CertificatePrintQueueItemMapper.create(requestDto);
        assertEquals(requestDto.getCity(), actual.getCity());
    }
    @Test
    void shouldMapLanguage(){
        var requestDto = fixture.create(CertificatePrintRequestDto.class);
        var actual = CertificatePrintQueueItemMapper.create(requestDto);
        assertEquals(requestDto.getLanguage(), actual.getLanguage());
    }
    @Test
    void shouldMapCantonCodeSender(){
        var requestDto = fixture.create(CertificatePrintRequestDto.class);
        var actual = CertificatePrintQueueItemMapper.create(requestDto);
        assertEquals(requestDto.getCantonCodeSender(), actual.getCantonCodeSender());
    }
    @Test
    void shouldMapIsBillable(){
        var requestDto = fixture.create(CertificatePrintRequestDto.class);
        var actual = CertificatePrintQueueItemMapper.create(requestDto);
        assertEquals(requestDto.getIsBillable(), actual.getIsBillable());
    }
    @Test
    void shouldMapCertificatePdfData(){
        var requestDto = fixture.create(CertificatePrintRequestDto.class);
        var actual = CertificatePrintQueueItemMapper.create(requestDto);
        assertEquals(requestDto.getPdfCertificate(), actual.getCertificatePdfData().getPdf());
    }
    @Test
    void shouldSetCreatedAtToCurrentTimestamp(){
        var requestDto = fixture.create(CertificatePrintRequestDto.class);
        var now = fixture.create(LocalDateTime.class);

        try (MockedStatic<LocalDateTime> localDateTimeMock = Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS)) {
            localDateTimeMock.when(LocalDateTime::now).thenReturn(now);
            var actual = CertificatePrintQueueItemMapper.create(requestDto);
            assertEquals(now, actual.getCreatedAt());
        }
    }
    @Test
    void shouldSetModifiedAtToCurrentTimestamp(){
        var requestDto = fixture.create(CertificatePrintRequestDto.class);
        var now = fixture.create(LocalDateTime.class);

        try (MockedStatic<LocalDateTime> localDateTimeMock = Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS)) {
            localDateTimeMock.when(LocalDateTime::now).thenReturn(now);
            var actual = CertificatePrintQueueItemMapper.create(requestDto);
            assertEquals(now, actual.getModifiedAt());
        }
    }
    @Test
    void shouldSetErrorCountToZero(){
        var requestDto = fixture.create(CertificatePrintRequestDto.class);
        var actual = CertificatePrintQueueItemMapper.create(requestDto);
        assertEquals(0, actual.getErrorCount());
    }
}