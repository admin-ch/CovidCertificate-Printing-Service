package ch.admin.bag.covidcertificate.service;

import ch.admin.bag.covidcertificate.domain.CertificatePrintMetadata;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Component
public class CsvWriterService {
    public void writeRowsToCsv(File file, List<CertificatePrintMetadata> rows)
            throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        FileWriter writer = new FileWriter(file);
        var mappingStrategy = new ColumnPositionMappingStrategy<CertificatePrintMetadata>();
        mappingStrategy.setType(CertificatePrintMetadata.class);

        var builder = new StatefulBeanToCsvBuilder<CertificatePrintMetadata>(writer);
        var beanWriter = builder
                .withMappingStrategy(mappingStrategy)
                .withApplyQuotesToAll(false)
                .withSeparator('|')
                .withLineEnd("|\r\n")
                .build();

        writeHeader(beanWriter);
        beanWriter.write(rows);
        writer.close();
    }

    private void writeHeader(StatefulBeanToCsv<CertificatePrintMetadata> beanWriter) throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        beanWriter.write(
                new CertificatePrintMetadata("UVCI",
                        "ADRESSZEILE_1",
                        "ADRESSZEILE_2",
                        "ADRESSZEILE_3",
                        "ADRESSZEILE_4",
                        "ADRESSE_PLZ",
                        "ADRESSE_ORT",
                        "ABSENDER",
                        "SPRACHE",
                        "PRIO",
                        "BEILAGE"));
    }
}
