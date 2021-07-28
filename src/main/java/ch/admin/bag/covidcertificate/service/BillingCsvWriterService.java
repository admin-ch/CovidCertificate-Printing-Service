package ch.admin.bag.covidcertificate.service;

import ch.admin.bag.covidcertificate.domain.AggregatedBillingKpi;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class BillingCsvWriterService {
    public void writeRowsToCsv(File file, List<AggregatedBillingKpi> rows)
            throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        var writer = new FileWriter(file);

        var builder = new StatefulBeanToCsvBuilder<AggregatedBillingKpi>(writer);
        var beanWriter = builder
                .withApplyQuotesToAll(false)
                .withSeparator('|')
                .withLineEnd("|\r\n")
                .build();

        writeHeader(Arrays.asList("Canton", "Date", "Count"), writer);
        beanWriter.write(rows);
        writer.close();
    }

    private void writeHeader(List<String> headers, FileWriter writer) throws IOException {
        writer.write("|");
        writer.write(String.join("|", headers));
        writer.write("|\n");
    }
}
