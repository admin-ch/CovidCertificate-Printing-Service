package ch.admin.bag.covidcertificate.domain;

import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ch.admin.bag.covidcertificate.domain.UvciUtils.mapFilenameFromUVCI;

@Component
public class CertificatePrintMetadataMapper {
    private static Map<String, String> cantonToSenderAddressMap;

    @PostConstruct
    private static void init() throws IOException {
        try (Reader reader = Files.newBufferedReader(new ClassPathResource("senderAddress.csv").getFile().toPath())) {
            var csvToBean = new CsvToBeanBuilder<SenderAddressCsvData>(reader)
                    .withType(SenderAddressCsvData.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withSeparator(';')
                    .build();

            cantonToSenderAddressMap = csvToBean.stream()
                    .collect(Collectors.toMap(SenderAddressCsvData::getCantonCode, SenderAddressCsvData::getAddress));
        }
    }

    public List<CertificatePrintMetadata> mapAll(Collection<CertificatePrintQueueItem> certificatePrintQueueItems){
        return certificatePrintQueueItems.stream()
                .map(this::map)
                .collect(Collectors.toList());
    }

    private CertificatePrintMetadata map(CertificatePrintQueueItem certificatePrintQueueItem){
        return new CertificatePrintMetadata(
                certificatePrintQueueItem.getUvci(),
                certificatePrintQueueItem.getAddressLine1(),
                certificatePrintQueueItem.getAddressLine2(),
                null,
                null,
                String.valueOf(certificatePrintQueueItem.getZipCode()),
                certificatePrintQueueItem.getCity(),
                cantonToSenderAddressMap.get(certificatePrintQueueItem.getCantonCodeSender().toUpperCase()),
                certificatePrintQueueItem.getLanguage(),
                "1",
                mapFilenameFromUVCI(certificatePrintQueueItem.getUvci())
        );
    }
}
