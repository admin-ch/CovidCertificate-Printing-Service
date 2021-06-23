package ch.admin.bag.covidcertificate.domain;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SenderAddressCsvData {
    @CsvBindByName(column = "CantonCode", required = true)
    private String cantonCode;

    @CsvBindByName(column = "Address", required = true)
    private String address;
}
