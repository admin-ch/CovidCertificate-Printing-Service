package ch.admin.bag.covidcertificate.api;

import ch.admin.bag.covidcertificate.api.error.InputValidationError;
import ch.admin.bag.covidcertificate.api.error.InputValidationException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class CertificatePrintRequestDto {
    private static final int PDF_SIZE_50KB = 50000;
    private static final int PDF_SIZE_250KB = 250000;

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
    private Boolean isBillable;

    public void validate(){
        if(pdfCertificate.length< PDF_SIZE_50KB || pdfCertificate.length> PDF_SIZE_250KB){
            throw new InputValidationException(
                    new InputValidationError(
                            this.getClass(),
                            "pdfCertificate",
                            "pdf size should be between 50 and 250 KB. Given pdf is "+pdfCertificate.length/1000+"KB."
                    ));
        }
    }
}