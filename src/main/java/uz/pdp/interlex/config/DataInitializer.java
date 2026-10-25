package uz.pdp.interlex.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;
    private final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Value("${app.admin.email}")
    private String adminEmail;

    // Muhit o'zgaruvchisi: APP_ADMIN_PASSWORD
    @Value("${app.admin.password:}")
    private String adminPassword;

    // Faqat prod bo'lmagan muhitda demo advokat uchun
    @Value("${app.demo.password:demo12345}")
    private String demoPassword;

    @PostConstruct
    @Transactional
    public void init() {
        try {
            ensureAdmin();
            if (!environment.acceptsProfiles(Profiles.of("prod")) && lawyerRepository.count() == 0) {
                Lawyer demo = Lawyer.builder()
                        .firstName("Demo")
                        .lastName("Lawyer")
                        .email("lawyer@example.com")
                        .phone("+998901234567")
                        .specializations(Set.of(ContactMessage.ServiceType.CIVIL, ContactMessage.ServiceType.FAMILY))
                        .yearsOfExperience(5)
                        .isActive(true)
                        .maxCasesPerDay(5)
                        .currentCaseCount(0)
                        .passwordHash(passwordEncoder.encode(demoPassword))
                        .build();
                lawyerRepository.save(demo);
                log.info("Created demo lawyer: {}", demo.getEmail());
            }
        } catch (Exception ex) {
            // Log and continue startup; initialization is best-effort
            log.error("DataInitializer failed: {}", ex.getMessage(), ex);
        }
    }

    /**
     * Admin advokat mavjud bo'lmasa yaratadi, parolsiz bo'lsa APP_ADMIN_PASSWORD'dan parol o'rnatadi.
     * Mavjud paroli bor adminning paroli bu yerda o'zgartirilmaydi.
     */
    private void ensureAdmin() {
        if (adminPassword == null || adminPassword.isBlank()) {
            log.warn("APP_ADMIN_PASSWORD berilmagan: admin ({}) tizimga kira olmaydi.", adminEmail);
            return;
        }
        if (adminPassword.length() < 8) {
            log.error("APP_ADMIN_PASSWORD kamida 8 belgidan iborat bo'lishi kerak. Admin yaratilmadi.");
            return;
        }
        Lawyer admin = lawyerRepository.findByEmail(adminEmail).orElse(null);
        if (admin == null) {
            admin = Lawyer.builder()
                    .firstName("Admin")
                    .lastName("MaxLegal")
                    .email(adminEmail)
                    .specializations(Set.of())
                    .isActive(true)
                    .maxCasesPerDay(0)
                    .currentCaseCount(0)
                    .build();
            log.info("Created admin account: {}", adminEmail);
        }
        if (admin.getPasswordHash() == null || admin.getPasswordHash().isBlank()) {
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));
            lawyerRepository.save(admin);
            log.info("Admin password set for: {}", adminEmail);
        }
    }
}
