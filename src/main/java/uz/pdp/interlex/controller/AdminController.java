package uz.pdp.interlex.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate; // Yangi import
import org.springframework.scheduling.annotation.Scheduled; // Yangi import
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.pdp.interlex.dto.AdminStatsDto;
import uz.pdp.interlex.dto.ApiResponse;
import uz.pdp.interlex.dto.LawyerStatsDto;
import uz.pdp.interlex.dto.ServiceTypeStatsDto;
import uz.pdp.interlex.entity.ContactMessage;
import uz.pdp.interlex.repo.ContactMessageRepository;
import uz.pdp.interlex.repo.LawyerRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ContactMessageRepository contactMessageRepo;
    private final LawyerRepository lawyerRepo;
    private final SimpMessagingTemplate messagingTemplate; // Yangi

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminStatsDto>> getAdminStats() {
        try {
            // Asosiy statistikalar
            Long totalClients = contactMessageRepo.countAllMessages();
            Long totalLawyers = lawyerRepo.countActiveLawyers();
            Double avgExperience = lawyerRepo.findAverageExperience();
            Long successfulCases = contactMessageRepo.countSuccessfulCases();

            // So'nggi 24 soatdagi ishlar
            LocalDateTime last24h = LocalDateTime.now().minusHours(24);
            Long casesLast24h = contactMessageRepo.countCasesSince(last24h);

            // Jarayondagi ishlar
            Long pendingCases = contactMessageRepo.countByStatus(ContactMessage.MessageStatus.IN_PROGRESS);

            // Muvaffaqiyat foizi
            Double successRate = totalClients > 0 ?
                    (successfulCases.doubleValue() / totalClients.doubleValue()) * 100 : 0.0;

            AdminStatsDto stats = AdminStatsDto.builder()
                    .totalClients(totalClients)
                    .totalLawyers(totalLawyers)
                    .avgExperience(avgExperience != null ? avgExperience : 0.0)
                    .successfulCases(successfulCases)
                    .casesLast24h(casesLast24h)
                    .pendingCases(pendingCases)
                    .successRate(Math.round(successRate * 100.0) / 100.0) // 2 xona aniqlik
                    .build();

            return ResponseEntity.ok(ApiResponse.success("Admin statistikasi", stats));
        } catch (Exception e) {
            log.error("Admin statistikasini olishda xatolik", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Statistikani olishda xatolik yuz berdi"));
        }
    }

    // Har 10 soniyada statistikani WebSocket orqali yuborish
    @Scheduled(fixedRate = 10000) // 10 soniya
    public void sendAdminStatsUpdate() {
        try {
            Long totalClients = contactMessageRepo.countAllMessages();
            Long totalLawyers = lawyerRepo.countActiveLawyers();
            Double avgExperience = lawyerRepo.findAverageExperience();
            Long successfulCases = contactMessageRepo.countSuccessfulCases();
            LocalDateTime last24h = LocalDateTime.now().minusHours(24);
            Long casesLast24h = contactMessageRepo.countCasesSince(last24h);
            Long pendingCases = contactMessageRepo.countByStatus(ContactMessage.MessageStatus.IN_PROGRESS);
            Double successRate = totalClients > 0 ?
                    (successfulCases.doubleValue() / totalClients.doubleValue()) * 100 : 0.0;

            AdminStatsDto stats = AdminStatsDto.builder()
                    .totalClients(totalClients)
                    .totalLawyers(totalLawyers)
                    .avgExperience(avgExperience != null ? avgExperience : 0.0)
                    .successfulCases(successfulCases)
                    .casesLast24h(casesLast24h)
                    .pendingCases(pendingCases)
                    .successRate(Math.round(successRate * 100.0) / 100.0)
                    .build();

            messagingTemplate.convertAndSend("/topic/admin/stats", ApiResponse.success("Realtime admin statistikasi", stats));
            log.debug("Admin stats sent via WebSocket");
        } catch (Exception e) {
            log.error("Error sending admin stats via WebSocket", e);
        }
    }

    @GetMapping("/stats/lawyers")
    public ResponseEntity<ApiResponse<List<LawyerStatsDto>>> getLawyerStats() {
        try {
            List<Object[]> rawStats = lawyerRepo.getLawyerStats();

            List<LawyerStatsDto> stats = rawStats.stream()
                    .map(data -> {
                        Long lawyerId = (Long) data[0];
                        String firstName = (String) data[1];
                        String lastName = (String) data[2];
                        String email = (String) data[3];
                        Long totalCases = (Long) data[4];
                        Long completedCases = (Long) data[5];

                        Double completionRate = totalCases > 0 ?
                                (completedCases.doubleValue() / totalCases.doubleValue()) * 100 : 0.0;

                        return new LawyerStatsDto(
                                lawyerId,
                                firstName + " " + lastName,
                                email,
                                totalCases,
                                completedCases,
                                Math.round(completionRate * 100.0) / 100.0
                        );
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Advokatlar statistikasi", stats));
        } catch (Exception e) {
            log.error("Advokatlar statistikasini olishda xatolik", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Advokat statistikasini olishda xatolik"));
        }
    }

    @GetMapping("/stats/service-types")
    public ResponseEntity<ApiResponse<List<ServiceTypeStatsDto>>> getServiceTypeStats() {
        try {
            List<Object[]> rawStats = contactMessageRepo.countCasesByServiceType();
            Long totalCases = contactMessageRepo.countAllMessages();

            List<ServiceTypeStatsDto> stats = rawStats.stream()
                    .map(data -> {
                        ContactMessage.ServiceType serviceType = (ContactMessage.ServiceType) data[0];
                        Long caseCount = (Long) data[1];

                        Double percentage = totalCases > 0 ?
                                (caseCount.doubleValue() / totalCases.doubleValue()) * 100 : 0.0;

                        return new ServiceTypeStatsDto(
                                serviceType.name(),
                                serviceType.getDisplayName(),
                                caseCount,
                                Math.round(percentage * 100.0) / 100.0
                        );
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Xizmat turlari statistikasi", stats));
        } catch (Exception e) {
            log.error("Xizmat turlari statistikasini olishda xatolik", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Xizmat turlari statistikasini olishda xatolik"));
        }
    }
}
