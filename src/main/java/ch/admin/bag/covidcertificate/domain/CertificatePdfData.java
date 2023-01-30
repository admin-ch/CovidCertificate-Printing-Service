package ch.admin.bag.covidcertificate.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Entity
@Table(name = "certificate_pdf_data")
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA
@AllArgsConstructor
@Getter
@Slf4j
@ToString
public class CertificatePdfData {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull
    private byte[] pdf;

    @OneToOne
    @JoinColumn(name = "certificate_pdf_print_queue_item_id")
    private CertificatePrintQueueItem certificatePrintQueueItem;

    public CertificatePdfData(@NotNull byte[] pdf, @NotNull CertificatePrintQueueItem certificatePrintQueueItem) {
        this.pdf = pdf;
        this.certificatePrintQueueItem = certificatePrintQueueItem;
    }
}
