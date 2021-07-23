package ch.admin.bag.covidcertificate.api;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class CertificatePrintRequestDto {
    @NotNull
    private byte[] pdfCertificate;

    @NotNull
    private String uvci;

    private String addressLine1;
    private String addressLine2;
    private int zipCode;
    private String city;
    private String language;
    private String cantonCodeSender;
    private LocalDate vaccinationDate;
}
