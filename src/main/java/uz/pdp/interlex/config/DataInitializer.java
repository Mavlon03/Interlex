package uz.pdp.interlex.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.interlex.entity.ContactMessage;
import uz.pdp.interlex.entity.Lawyer;
import uz.pdp.interlex.repo.LawyerRepository;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final LawyerRepository lawyerRepository;
    private final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @PostConstruct
    @Transactional
    public void init() {
        try {
            if (lawyerRepository.count() == 0) {
                Lawyer l1 = Lawyer.builder()
                        .firstName("Demo")
                        .lastName("Lawyer")
                        .email("lawyer@example.com")
                        .phone("+998901234567")
                        .specializations(Set.of(ContactMessage.ServiceType.CIVIL, ContactMessage.ServiceType.FAMILY))
                        .yearsOfExperience(5)
                        .isActive(true)
                        .maxCasesPerDay(5)
                        .currentCaseCount(0)
                        .build();
                lawyerRepository.save(l1);
                log.info("Created demo lawyer: {}", l1.getEmail());
            }
        } catch (Exception ex) {
            // Log and continue startup; initialization is best-effort
            log.error("DataInitializer failed: {}", ex.getMessage(), ex);
        }
    }
}