package ch.admin.bag.covidcertificate.domain;

import com.flextrade.jfixture.JFixture;
import org.junit.Ignore;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.url=jdbc:h2:mem:testDb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.username=sa",
        "spring.datasource.password=sa",
        "spring.flyway.clean-on-validation-error=true"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@ActiveProfiles({"local"})
@ExtendWith(SpringExtension.class)
@Ignore
class CertificatePrintQueueRepositoryTest {
    @Autowired
    private CertificatePrintQueueRepository repository;
    @PersistenceContext
    private EntityManager entityManager;

    private final JFixture fixture = new JFixture();

//    @Nested
//    class GetNotProcessedItems {
//        @Test
//        @Transactional
//        void shouldReturnEmptyPage_whenNoUnprocessedItemsInTheDB() {
//            persist(CertificatePrintStatus.PROCESSED);
//            Pageable p = PageRequest.of(0,20);
//            Page<CertificatePrintQueueItem> result = repository.getNotProcessedItems(LocalDateTime.now().plusYears(10), p);
//
//            assertThat(result).isEmpty();
//        }
//
//        @Test
//        @Transactional
//        void shouldReturnRequestedPage_whenUnprocessedItemsExistInTheDB() {
//            int pageSize = 20;
//            persist(CertificatePrintStatus.CREATED, 30);
//            Pageable page1 = PageRequest.of(0,pageSize);
//            Pageable page2 = PageRequest.of(1,pageSize);
//            Page<CertificatePrintQueueItem> resultPage1 = repository.getNotProcessedItems(LocalDateTime.now().plusYears(10), page1);
//            Page<CertificatePrintQueueItem> resultPage2 = repository.getNotProcessedItems(LocalDateTime.now().plusYears(10), page2);
//
//            assertEquals(pageSize,resultPage1.toList().size());
//            assertEquals(10,resultPage2.toList().size());
//        }
//
//        @Test
//        @Transactional
//        void shouldLoadItemCorrectly_whenUnprocessedItemsExistInTheDB() {
//            CertificatePrintQueueItem expected = persist(CertificatePrintStatus.CREATED);
//            Pageable p = PageRequest.of(0,20);
//            Page<CertificatePrintQueueItem> result = repository.getNotProcessedItems(LocalDateTime.now().plusYears(10), p);
//
//            assertEquals(1, result.toList().size());
//            assertEquals(expected, result.getContent().get(0));
//        }
//    }


    private List<CertificatePrintQueueItem> persist(CertificatePrintStatus status, int numberOfItems) {
        List<CertificatePrintQueueItem> items = new ArrayList<>();
        for(int i=0; i < numberOfItems; i++){
            items.add(persist(status));
        }
        return items;
    }

    private CertificatePrintQueueItem persist(CertificatePrintStatus status) {
        CertificatePrintQueueItem certificatePrintQueueItem = fixture.create(CertificatePrintQueueItem.class);
        certificatePrintQueueItem.setStatus(status.name());
        ReflectionTestUtils.setField(certificatePrintQueueItem, "createdAt", LocalDateTime.now());
        entityManager.persist(certificatePrintQueueItem);
        return certificatePrintQueueItem;
    }
}
