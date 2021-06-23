package ch.admin.bag.covidcertificate.domain;

import com.opencsv.bean.CsvBindByPosition;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CertificatePrintMetadata {
    @CsvBindByPosition(position = 0)
    private String uvci;

    @CsvBindByPosition(position = 1)
    private String addressLine1;

    @CsvBindByPosition(position = 2)
    private String addressLine2;

    @CsvBindByPosition(position = 3)
    private String addressLine3;

    @CsvBindByPosition(position = 4)
    private String addressLine4;

    @CsvBindByPosition(position = 5)
    private String zipCode;

    @CsvBindByPosition(position = 6)
    private String city;

    @CsvBindByPosition(position = 7)
    private String sender;

    @CsvBindByPosition(position = 8)
    private String language;

    @CsvBindByPosition(position = 9)
    private String prio;

    @CsvBindByPosition(position = 10)
    private String filename;
}
