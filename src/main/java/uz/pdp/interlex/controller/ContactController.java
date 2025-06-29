package uz.pdp.interlex.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.interlex.dto.ApiResponse;
import uz.pdp.interlex.dto.ContactMessageDto;
import uz.pdp.interlex.entity.ContactMessage;
import uz.pdp.interlex.service.ContactMessageService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor

public class ContactController {

    private final ContactMessageService contactMessageService;

    @PostMapping
    public ResponseEntity<ApiResponse<ContactMessage>> createContactMessage(
            @Valid @RequestBody ContactMessageDto dto,
            HttpServletRequest request) {

        log.info("Received contact message from: {}", dto.getEmail());

        try {
            ContactMessage savedMessage = contactMessageService.createContactMessage(dto, request);

            return ResponseEntity.ok(
                    ApiResponse.success("Xabaringiz muvaffaqiyatli yuborildi! Tez orada siz bilan bog'lanamiz.", savedMessage)
            );
        } catch (Exception e) {
            log.error("Error creating contact message", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Xabar yuborishda xatolik yuz berdi. Iltimos, keyinroq qayta urinib ko'ring."));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ContactMessage>> getContactMessage(@PathVariable Long id) {
        try {
            ContactMessage message = contactMessageService.findById(id);
            return ResponseEntity.ok(ApiResponse.success("Xabar topildi", message));
        } catch (Exception e) {
            log.error("Error finding contact message with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Xabar topilmadi"));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ContactMessage>>> getAllContactMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {

        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<ContactMessage> messages;

            if (search != null && !search.trim().isEmpty()) {
                messages = contactMessageService.searchMessages(search.trim(), pageable);
            } else if (status != null && !status.trim().isEmpty()) {
                ContactMessage.MessageStatus messageStatus = ContactMessage.MessageStatus.valueOf(status.toUpperCase());
                messages = contactMessageService.findByStatus(messageStatus, pageable);
            } else {
                messages = contactMessageService.searchMessages("", pageable);
            }

            return ResponseEntity.ok(ApiResponse.success("Xabarlar ro'yxati", messages));
        } catch (Exception e) {
            log.error("Error getting contact messages", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Xabarlarni olishda xatolik yuz berdi"));
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<ContactMessage>>> getMessagesByStatus(@PathVariable String status) {
        try {
            ContactMessage.MessageStatus messageStatus = ContactMessage.MessageStatus.valueOf(status.toUpperCase());
            List<ContactMessage> messages = contactMessageService.findByStatus(messageStatus);
            return ResponseEntity.ok(ApiResponse.success("Xabarlar topildi", messages));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Noto'g'ri status: " + status));
        } catch (Exception e) {
            log.error("Error getting messages by status: {}", status, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Xabarlarni olishda xatolik yuz berdi"));
        }
    }

    @GetMapping("/service-type/{serviceType}")
    public ResponseEntity<ApiResponse<List<ContactMessage>>> getMessagesByServiceType(@PathVariable String serviceType) {
        try {
            ContactMessage.ServiceType type = ContactMessage.ServiceType.valueOf(serviceType.toUpperCase());
            List<ContactMessage> messages = contactMessageService.findByServiceType(type);
            return ResponseEntity.ok(ApiResponse.success("Xabarlar topildi", messages));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Noto'g'ri xizmat turi: " + serviceType));
        } catch (Exception e) {
            log.error("Error getting messages by service type: {}", serviceType, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Xabarlarni olishda xatolik yuz berdi"));
        }
    }

    @GetMapping("/lawyer/{lawyerId}")
    public ResponseEntity<ApiResponse<List<ContactMessage>>> getMessagesByLawyer(@PathVariable Long lawyerId) {
        try {
            List<ContactMessage> messages = contactMessageService.findByLawyerId(lawyerId);
            return ResponseEntity.ok(ApiResponse.success("Advokat xabarlari topildi", messages));
        } catch (Exception e) {
            log.error("Error getting messages by lawyer: {}", lawyerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Xabarlarni olishda xatolik yuz berdi"));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ContactMessage>> updateMessageStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        try {
            ContactMessage.MessageStatus messageStatus = ContactMessage.MessageStatus.valueOf(status.toUpperCase());
            ContactMessage updatedMessage = contactMessageService.updateStatus(id, messageStatus);
            return ResponseEntity.ok(ApiResponse.success("Xabar holati yangilandi", updatedMessage));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Noto'g'ri status: " + status));
        } catch (Exception e) {
            log.error("Error updating message status: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Xabar holatini yangilashda xatolik yuz berdi"));
        }
    }

    @PutMapping("/{messageId}/assign/{lawyerId}")
    public ResponseEntity<ApiResponse<ContactMessage>> assignMessageToLawyer(
            @PathVariable Long messageId,
            @PathVariable Long lawyerId) {

        try {
            ContactMessage updatedMessage = contactMessageService.assignToLawyer(messageId, lawyerId);
            return ResponseEntity.ok(ApiResponse.success("Xabar advokatga tayinlandi", updatedMessage));
        } catch (Exception e) {
            log.error("Error assigning message {} to lawyer {}", messageId, lawyerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Xabarni advokatga tayinlashda xatolik yuz berdi"));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Object>> getContactStats() {
        try {
            var stats = new Object() {
                public final Long newMessages = contactMessageService.countByStatus(ContactMessage.MessageStatus.NEW);
                public final Long inProgress = contactMessageService.countByStatus(ContactMessage.MessageStatus.IN_PROGRESS);
                public final Long responded = contactMessageService.countByStatus(ContactMessage.MessageStatus.RESPONDED);
                public final Long closed = contactMessageService.countByStatus(ContactMessage.MessageStatus.CLOSED);
                public final Long total = newMessages + inProgress + responded + closed;
            };

            return ResponseEntity.ok(ApiResponse.success("Statistika ma'lumotlari", stats));
        } catch (Exception e) {
            log.error("Error getting contact stats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Statistika ma'lumotlarini olishda xatolik yuz berdi"));
        }
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<ContactMessage>>> getRecentMessages(
            @RequestParam(defaultValue = "10") int limit) {

        try {
            Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());
            Page<ContactMessage> messages = contactMessageService.searchMessages("", pageable);
            return ResponseEntity.ok(ApiResponse.success("So'nggi xabarlar", messages.getContent()));
        } catch (Exception e) {
            log.error("Error getting recent messages", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("So'nggi xabarlarni olishda xatolik yuz berdi"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(@PathVariable Long id) {
        try {
            contactMessageService.deleteMessage(id);
            return ResponseEntity.ok(ApiResponse.success("Xabar o'chirildi"));
        } catch (Exception e) {
            log.error("Error deleting message: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Xabarni o'chirishda xatolik yuz berdi"));
        }
    }
}
