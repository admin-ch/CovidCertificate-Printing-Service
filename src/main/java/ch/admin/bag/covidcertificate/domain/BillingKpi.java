package ch.admin.bag.covidcertificate.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "billing_kpi")
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA
@AllArgsConstructor
@Getter
@Slf4j
public class BillingKpi {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String cantonCodeSender;

    private String uvci;

    private LocalDateTime processedAt;

    public BillingKpi(String cantonCodeSender, String uvci, LocalDateTime processedAt){
        this.cantonCodeSender = cantonCodeSender;
        this.uvci = uvci;
        this.processedAt = processedAt;
    }
}
