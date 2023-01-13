package ch.admin.bag.covidcertificate.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.List;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BillingKpiMapper {

    public static List<BillingKpi> mapAll(Collection<CertificatePrintQueueItem> certificatePrintQueueItems){
        return  certificatePrintQueueItems.stream().map(BillingKpiMapper::map).toList();
    }

    private static BillingKpi map(CertificatePrintQueueItem certificatePrintQueueItem){
        return new BillingKpi(
                certificatePrintQueueItem.getCantonCodeSender(),
                certificatePrintQueueItem.getUvci(),
                certificatePrintQueueItem.getModifiedAt(),
                certificatePrintQueueItem.getIsBillable()
        );
    }
}
