package ch.admin.bag.covidcertificate.domain;

import com.opencsv.bean.CsvBindByPosition;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class AggregatedBillingKpi {
    @CsvBindByPosition(position = 1)
    private String cantonCodeSender;

    @CsvBindByPosition(position = 2)
    private Date processedAt;

    @CsvBindByPosition(position = 3)
    private Long count;
}
