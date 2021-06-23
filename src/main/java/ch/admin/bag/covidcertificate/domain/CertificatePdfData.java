package ch.admin.bag.covidcertificate.domain;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
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
