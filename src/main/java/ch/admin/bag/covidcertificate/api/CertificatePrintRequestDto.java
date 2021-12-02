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
import javax.validation.constraints.Size;

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
    private Boolean isBillable;

    public void validate(){
        final int _50KB = 50000;
        final int _250KB = 250000;

        if(pdfCertificate.length<_50KB || pdfCertificate.length>_250KB){
            throw new InputValidationException(
                    new InputValidationError(
                            this.getClass(),
                            "pdfCertificate",
                            "pdf size should be between 50 and 250 KB. Given pdf is "+pdfCertificate.length/1000+"KB."
                    ));
        }
    }
}