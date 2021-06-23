package ch.admin.bag.covidcertificate.api;

import ch.admin.bag.covidcertificate.domain.CertificatePrintQueueItem;
import ch.admin.bag.covidcertificate.domain.CertificatePrintStatus;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CertificatePrintQueueItemMapper {

    public static CertificatePrintQueueItem create(CertificatePrintRequestDto printCertificateRequestDto){
        return new CertificatePrintQueueItem(
                printCertificateRequestDto.getUvci(),
                CertificatePrintStatus.CREATED.name(),
                printCertificateRequestDto.getAddressLine1(),
                printCertificateRequestDto.getAddressLine2(),
                printCertificateRequestDto.getZipCode(),
                printCertificateRequestDto.getCity(),
                printCertificateRequestDto.getLanguage(),
                printCertificateRequestDto.getCantonCodeSender(),
                printCertificateRequestDto.getPdfCertificate()
        );
    }
}
