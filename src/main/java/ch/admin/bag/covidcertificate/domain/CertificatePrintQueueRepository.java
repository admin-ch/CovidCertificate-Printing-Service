package ch.admin.bag.covidcertificate.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface CertificatePrintQueueRepository extends JpaRepository<CertificatePrintQueueItem, UUID>, JpaSpecificationExecutor<CertificatePrintQueueItem> {

    @Query("SELECT printItem " +
            "FROM CertificatePrintQueueItem printItem " +
            "WHERE printItem.status = 'CREATED' " +
            "AND printItem.createdAt <= :createdBefore")
    Page<CertificatePrintQueueItem> getNotProcessedItems(@Param("createdBefore") LocalDateTime createdBefore, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM CertificatePrintQueueItem item " +
            "WHERE item.status = 'PROCESSED' " +
            "AND item.modifiedAt < :modifiedBefore")
    int deleteItemsProcessedBeforeTimestamp(@Param("modifiedBefore") LocalDateTime modifiedBefore);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE CertificatePrintQueueItem item " +
            "SET item.status = 'CREATED', item.errorCount = 0" +
            "WHERE item.status = 'ERROR' ")
    int updateFailedAndResetErrorCount();
}
