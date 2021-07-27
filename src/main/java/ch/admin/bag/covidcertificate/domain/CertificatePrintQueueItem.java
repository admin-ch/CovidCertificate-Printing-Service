package ch.admin.bag.covidcertificate.domain;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.hibernate.annotations.CascadeType.PERSIST;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"uvci"}, name = "certificate_print_queue_item")})
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA
@AllArgsConstructor
@Getter
@Slf4j
@ToString(exclude = "certificatePdfData")
public class CertificatePrintQueueItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull
    private String uvci;

    @NotNull
    @Setter
    private String status;

    private String addressLine1;
    private String addressLine2;
    private Integer zipCode;
    private String city;
    private String language;
    private String cantonCodeSender;
    private Boolean isBillable;
    @Setter
    private Integer errorCount;

    @OneToOne(mappedBy = "certificatePrintQueueItem")
    @Cascade(PERSIST)
    private CertificatePdfData certificatePdfData;

    @Column(name = "created_at", insertable = false)
    private LocalDateTime createdAt;

    @Setter
    @Column(name = "modified_at", insertable = false)
    private LocalDateTime modifiedAt;


    public CertificatePrintQueueItem(String uvci, String status,
                                     String addressLine1, String addressLine2, Integer zipCode, String city,
                                     String language, String cantonCodeSender, Boolean isBillable, byte[] pdfData) {
        this.uvci = uvci;
        this.status = status;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.zipCode = zipCode;
        this.city = city;
        this.language = language;
        this.cantonCodeSender = cantonCodeSender;
        this.isBillable = isBillable;
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = LocalDateTime.now();
        this.errorCount = 0;
        this.certificatePdfData = new CertificatePdfData(pdfData, this);
    }
}
