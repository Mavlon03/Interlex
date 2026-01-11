package uz.pdp.interlex.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.interlex.dto.ApiResponse;
import uz.pdp.interlex.dto.LawyerDto;
import uz.pdp.interlex.entity.ContactMessage;
import uz.pdp.interlex.entity.Lawyer;
import uz.pdp.interlex.service.LawyerService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/lawyers")
@RequiredArgsConstructor
public class LawyerController {

    private final LawyerService lawyerService;

    @PostMapping
    public ResponseEntity<ApiResponse<Lawyer>> createLawyer(@Valid @RequestBody LawyerDto dto) {
        try {
            Lawyer savedLawyer = lawyerService.createLawyer(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Advokat muvaffaqiyatli yaratildi", savedLawyer));
        } catch (Exception e) {
            log.error("Error creating lawyer", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Advokat yaratishda xatolik yuz berdi"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Lawyer>> getLawyer(@PathVariable Long id) {
        try {
            Lawyer lawyer = lawyerService.findById(id);
            return ResponseEntity.ok(ApiResponse.success("Advokat topildi", lawyer));
        } catch (Exception e) {
            log.error("Error finding lawyer with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Advokat topilmadi"));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Lawyer>>> getAllLawyers() {
        try {
            List<Lawyer> lawyers = lawyerService.findAllActive();
            return ResponseEntity.ok(ApiResponse.success("Advokatlar ro'yxati", lawyers));
        } catch (Exception e) {
            log.error("Error getting all lawyers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Advokatlar ro'yxatini olishda xatolik yuz berdi"));
        }
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<Lawyer>>> getAvailableLawyers() {
        try {
            List<Lawyer> lawyers = lawyerService.findAvailableLawyers();
            return ResponseEntity.ok(ApiResponse.success("Mavjud advokatlar", lawyers));
        } catch (Exception e) {
            log.error("Error getting available lawyers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Mavjud advokatlarni olishda xatolik yuz berdi"));
        }
    }

    @GetMapping("/specialization/{serviceType}")
    public ResponseEntity<ApiResponse<List<Lawyer>>> getLawyersBySpecialization(@PathVariable String serviceType) {
        try {
            ContactMessage.ServiceType type = ContactMessage.ServiceType.valueOf(serviceType.toUpperCase());
            List<Lawyer> lawyers = lawyerService.findBySpecialization(type);
            return ResponseEntity.ok(ApiResponse.success("Mutaxassis advokatlar", lawyers));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Noto'g'ri xizmat turi: " + serviceType));
        } catch (Exception e) {
            log.error("Error getting lawyers by specialization: {}", serviceType, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Mutaxassis advokatlarni olishda xatolik yuz berdi"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Lawyer>> updateLawyer(@PathVariable Long id, @Valid @RequestBody LawyerDto dto) {
        try {
            Lawyer updatedLawyer = lawyerService.updateLawyer(id, dto);
            return ResponseEntity.ok(ApiResponse.success("Advokat ma'lumotlari yangilandi", updatedLawyer));
        } catch (Exception e) {
            log.error("Error updating lawyer: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Advokat ma'lumotlarini yangilashda xatolik yuz berdi"));
        }
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activateLawyer(@PathVariable Long id) {
        try {
            lawyerService.activateLawyer(id);
            return ResponseEntity.ok(ApiResponse.success("Advokat faollashtirildi"));
        } catch (Exception e) {
            log.error("Error activating lawyer: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Advokatni faollashtirishda xatolik yuz berdi"));
        }
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateLawyer(@PathVariable Long id) {
        try {
            lawyerService.deactivateLawyer(id);
            return ResponseEntity.ok(ApiResponse.success("Advokat faolsizlantirildi"));
        } catch (Exception e) {
            log.error("Error deactivating lawyer: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Advokatni faolsizlantirishda xatolik yuz berdi"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLawyer(@PathVariable Long id) {
        try {
            lawyerService.deleteLawyer(id);
            return ResponseEntity.ok(ApiResponse.success("Advokat o'chirildi"));
        } catch (Exception e) {
            log.error("Error deleting lawyer: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Advokatni o'chirishda xatolik yuz berdi"));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Object>> getLawyerStats() {
        try {
            var stats = new Object() {
                public final Long totalLawyers = lawyerService.countActiveLawyers();
                public final Long availableLawyers = (long) lawyerService.findAvailableLawyers().size();
            };

            return ResponseEntity.ok(ApiResponse.success("Advokatlar statistikasi", stats));
        } catch (Exception e) {
            log.error("Error getting lawyer stats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Advokatlar statistikasini olishda xatolik yuz berdi"));
        }
    }
}
