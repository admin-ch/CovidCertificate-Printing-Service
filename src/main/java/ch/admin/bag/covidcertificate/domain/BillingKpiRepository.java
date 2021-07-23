package ch.admin.bag.covidcertificate.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BillingKpiRepository extends JpaRepository<BillingKpi, UUID>, JpaSpecificationExecutor<BillingKpi> {

    @Query("SELECT new ch.admin.bag.covidcertificate.domain.AggregatedBillingKpi(billingKpi.cantonCodeSender, date_trunc(billingKpi.processedAt), count(billingKpi))" +
            "FROM BillingKpi billingKpi " +
            "WHERE billingKpi.processedAt >= :processedAtSince " +
            "AND billingKpi.processedAt < :processedAtUntil " +
            "GROUP BY billingKpi.cantonCodeSender, date_trunc(billingKpi.processedAt)" )
    List<AggregatedBillingKpi> getBillingInformation(
            @Param("processedAtSince") LocalDateTime processedAtSince,
            @Param("processedAtUntil") LocalDateTime processedAtUntil);
}
