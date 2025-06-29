//package uz.pdp.interlex.service;
//
//import jakarta.servlet.http.HttpServletRequest;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import uz.pdp.interlex.dto.ContactMessageDto;
//import uz.pdp.interlex.entity.ContactMessage;
//import uz.pdp.interlex.entity.Lawyer;
//import uz.pdp.interlex.exeption.ResourceNotFoundException;
//import uz.pdp.interlex.repo.ContactMessageRepository;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class ContactMessageService {
//
//    private final ContactMessageRepository contactMessageRepository;
//    private final LawyerService lawyerService;
//    private final EmailService emailService;
//    private final NotificationService notificationService;
//
//    @Transactional
//    public ContactMessage createContactMessage(ContactMessageDto dto, HttpServletRequest request) {
//        log.info("Creating new contact message from: {}", dto.getEmail());
//
//        ContactMessage contactMessage = ContactMessage.builder()
//                .name(dto.getName())
//                .email(dto.getEmail())
//                .phone(dto.getPhone())
//                .serviceType(dto.getServiceTypeEnum())
//                .clientType(dto.getClientTypeEnum())
//                .subject(dto.getSubject())
//                .message(dto.getMessage())
//                .langCode(dto.getLangCode() != null ? dto.getLangCode() : "uz")
//                .status(ContactMessage.MessageStatus.NEW)
//                .clientIp(getClientIpAddress(request))
//                .userAgent(request.getHeader("User-Agent"))
//                .build();
//
//        ContactMessage savedMessage = contactMessageRepository.save(contactMessage);
//
//        // Auto-assign lawyer if available
//        assignLawyerToMessage(savedMessage);
//
//        // Send notifications
//        notificationService.sendNewMessageNotifications(savedMessage);
//
//        // Send auto-response email
//        emailService.sendAutoResponseEmail(savedMessage);
//
//        log.info("Contact message created successfully with ID: {}", savedMessage.getId());
//        return savedMessage;
//    }
//
//    @Transactional
//    public ContactMessage assignLawyerToMessage(ContactMessage message) {
//        try {
//            Lawyer availableLawyer = lawyerService.findBestAvailableLawyer(message.getServiceType());
//            if (availableLawyer != null) {
//                message.setAssignedLawyer(availableLawyer);
//                message.setStatus(ContactMessage.MessageStatus.IN_PROGRESS);
//
//                // Increment lawyer's current case count
//                lawyerService.incrementCaseCount(availableLawyer.getId());
//
//                log.info("Assigned lawyer {} to message {}", availableLawyer.getId(), message.getId());
//            }
//        } catch (Exception e) {
//            log.warn("Could not auto-assign lawyer to message {}: {}", message.getId(), e.getMessage());
//        }
//
//        return contactMessageRepository.save(message);
//    }
//
//    public ContactMessage findById(Long id) {
//        return contactMessageRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Contact message not found with id: " + id));
//    }
//
//    public List<ContactMessage> findByStatus(ContactMessage.MessageStatus status) {
//        return contactMessageRepository.findByStatusOrderByCreatedAtDesc(status);
//    }
//
//    public Page<ContactMessage> findByStatus(ContactMessage.MessageStatus status, Pageable pageable) {
//        return contactMessageRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
//    }
//
//    public List<ContactMessage> findByServiceType(ContactMessage.ServiceType serviceType) {
//        return contactMessageRepository.findByServiceTypeOrderByCreatedAtDesc(serviceType);
//    }
//
//    public List<ContactMessage> findByLawyerId(Long lawyerId) {
//        return contactMessageRepository.findByAssignedLawyerIdOrderByCreatedAtDesc(lawyerId);
//    }
//
//    public Page<ContactMessage> searchMessages(String keyword, Pageable pageable) {
//        return contactMessageRepository.searchMessages(keyword, pageable);
//    }
//
//    @Transactional
//    public ContactMessage updateStatus(Long id, ContactMessage.MessageStatus status) {
//        ContactMessage message = findById(id);
//        ContactMessage.MessageStatus oldStatus = message.getStatus();
//        message.setStatus(status);
//
//        ContactMessage updatedMessage = contactMessageRepository.save(message);
//
//        log.info("Updated message {} status from {} to {}", id, oldStatus, status);
//
//        // Send status update notifications if needed
//        if (status == ContactMessage.MessageStatus.RESPONDED || status == ContactMessage.MessageStatus.CLOSED) {
//            notificationService.sendStatusUpdateNotification(updatedMessage, oldStatus);
//        }
//
//        return updatedMessage;
//    }
//
//    @Transactional
//    public ContactMessage assignToLawyer(Long messageId, Long lawyerId) {
//        ContactMessage message = findById(messageId);
//        Lawyer lawyer = lawyerService.findById(lawyerId);
//
//        // Remove from previous lawyer if assigned
//        if (message.getAssignedLawyer() != null && !message.getAssignedLawyer().getId().equals(lawyerId)) {
//            lawyerService.decrementCaseCount(message.getAssignedLawyer().getId());
//        }
//
//        message.setAssignedLawyer(lawyer);
//        message.setStatus(ContactMessage.MessageStatus.IN_PROGRESS);
//
//        // Increment new lawyer's case count
//        lawyerService.incrementCaseCount(lawyerId);
//
//        ContactMessage updatedMessage = contactMessageRepository.save(message);
//
//        log.info("Assigned message {} to lawyer {}", messageId, lawyerId);
//
//        // Send assignment notification
//        notificationService.sendAssignmentNotification(updatedMessage);
//
//        return updatedMessage;
//    }
//
//    public List<ContactMessage> findMessagesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
//        return contactMessageRepository.findByCreatedAtBetween(startDate, endDate);
//    }
//
//    public Long countByStatus(ContactMessage.MessageStatus status) {
//        return contactMessageRepository.countByStatus(status);
//    }
//
//    public List<ContactMessage> findByEmail(String email) {
//        return contactMessageRepository.findByEmailOrderByCreatedAtDesc(email);
//    }
//
//    public List<ContactMessage> findByPhone(String phone) {
//        return contactMessageRepository.findByPhoneOrderByCreatedAtDesc(phone);
//    }
//
//    @Transactional
//    public void deleteMessage(Long id) {
//        ContactMessage message = findById(id);
//
//        // Decrement lawyer's case count if assigned
//        if (message.getAssignedLawyer() != null) {
//            lawyerService.decrementCaseCount(message.getAssignedLawyer().getId());
//        }
//
//        contactMessageRepository.deleteById(id);
//        log.info("Deleted contact message with ID: {}", id);
//    }
//
//    private String getClientIpAddress(HttpServletRequest request) {
//        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
//        if (xForwardedForHeader == null || xForwardedForHeader.isEmpty()) {
//            return request.getRemoteAddr();
//        } else {
//            return xForwardedForHeader.split(",")[0].trim();
//        }
//    }
//}

package uz.pdp.interlex.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate; // Yangi import
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.interlex.dto.ContactMessageDto;
import uz.pdp.interlex.entity.ContactMessage;
import uz.pdp.interlex.entity.Lawyer;
import uz.pdp.interlex.exeption.ResourceNotFoundException;
import uz.pdp.interlex.repo.ContactMessageRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactMessageService {

    private final ContactMessageRepository contactMessageRepository;
    private final LawyerService lawyerService;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate; // Yangi

    @Transactional
    public ContactMessage createContactMessage(ContactMessageDto dto, HttpServletRequest request) {
        log.info("Creating new contact message from: {}", dto.getEmail());

        ContactMessage contactMessage = ContactMessage.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .serviceType(dto.getServiceTypeEnum())
                .clientType(dto.getClientTypeEnum())
                .subject(dto.getSubject())
                .message(dto.getMessage())
                .langCode(dto.getLangCode() != null ? dto.getLangCode() : "uz")
                .status(ContactMessage.MessageStatus.NEW)
                .clientIp(getClientIpAddress(request))
                .userAgent(request.getHeader("User-Agent"))
                .build();

        ContactMessage savedMessage = contactMessageRepository.save(contactMessage);

        // Auto-assign lawyer if available
        assignLawyerToMessage(savedMessage);

        // Send notifications
        notificationService.sendNewMessageNotifications(savedMessage);

        // Send auto-response email
        emailService.sendAutoResponseEmail(savedMessage);

        // WebSocket orqali yangi xabar haqida xabar berish
        messagingTemplate.convertAndSend("/topic/messages/new", savedMessage);

        log.info("Contact message created successfully with ID: {}", savedMessage.getId());
        return savedMessage;
    }

    @Transactional
    public ContactMessage assignLawyerToMessage(ContactMessage message) {
        try {
            Lawyer availableLawyer = lawyerService.findBestAvailableLawyer(message.getServiceType());
            if (availableLawyer != null) {
                message.setAssignedLawyer(availableLawyer);
                message.setStatus(ContactMessage.MessageStatus.IN_PROGRESS);

                // Increment lawyer's current case count
                lawyerService.incrementCaseCount(availableLawyer.getId());

                log.info("Assigned lawyer {} to message {}", availableLawyer.getId(), message.getId());
            }
        } catch (Exception e) {
            log.warn("Could not auto-assign lawyer to message {}: {}", message.getId(), e.getMessage());
        }

        ContactMessage savedMessage = contactMessageRepository.save(message);
        // Holat o'zgarganligi haqida WebSocket orqali xabar berish
        messagingTemplate.convertAndSend("/topic/messages/statusUpdate", savedMessage);
        return savedMessage;
    }

    public ContactMessage findById(Long id) {
        return contactMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact message not found with id: " + id));
    }

    public List<ContactMessage> findByStatus(ContactMessage.MessageStatus status) {
        return contactMessageRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    public Page<ContactMessage> findByStatus(ContactMessage.MessageStatus status, Pageable pageable) {
        return contactMessageRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
    }

    public List<ContactMessage> findByServiceType(ContactMessage.ServiceType serviceType) {
        return contactMessageRepository.findByServiceTypeOrderByCreatedAtDesc(serviceType);
    }

    public List<ContactMessage> findByLawyerId(Long lawyerId) {
        return contactMessageRepository.findByAssignedLawyerIdOrderByCreatedAtDesc(lawyerId);
    }

    public Page<ContactMessage> searchMessages(String keyword, Pageable pageable) {
        return contactMessageRepository.searchMessages(keyword, pageable);
    }

    @Transactional
    public ContactMessage updateStatus(Long id, ContactMessage.MessageStatus status) {
        ContactMessage message = findById(id);
        ContactMessage.MessageStatus oldStatus = message.getStatus();
        message.setStatus(status);

        ContactMessage updatedMessage = contactMessageRepository.save(message);

        log.info("Updated message {} status from {} to {}", id, oldStatus, status);

        // Send status update notifications if needed
        if (status == ContactMessage.MessageStatus.RESPONDED || status == ContactMessage.MessageStatus.CLOSED) {
            notificationService.sendStatusUpdateNotification(updatedMessage, oldStatus);
        }
        // Holat o'zgarganligi haqida WebSocket orqali xabar berish
        messagingTemplate.convertAndSend("/topic/messages/statusUpdate", updatedMessage);

        return updatedMessage;
    }

    @Transactional
    public ContactMessage assignToLawyer(Long messageId, Long lawyerId) {
        ContactMessage message = findById(messageId);
        Lawyer lawyer = lawyerService.findById(lawyerId);

        // Remove from previous lawyer if assigned
        if (message.getAssignedLawyer() != null && !message.getAssignedLawyer().getId().equals(lawyerId)) {
            lawyerService.decrementCaseCount(message.getAssignedLawyer().getId());
        }

        message.setAssignedLawyer(lawyer);
        message.setStatus(ContactMessage.MessageStatus.IN_PROGRESS);

        // Increment new lawyer's case count
        lawyerService.incrementCaseCount(lawyerId);

        ContactMessage updatedMessage = contactMessageRepository.save(message);

        log.info("Assigned message {} to lawyer {}", messageId, lawyerId);

        // Send assignment notification
        notificationService.sendAssignmentNotification(updatedMessage);
        // Holat o'zgarganligi haqida WebSocket orqali xabar berish
        messagingTemplate.convertAndSend("/topic/messages/statusUpdate", updatedMessage);

        return updatedMessage;
    }

    public List<ContactMessage> findMessagesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return contactMessageRepository.findByCreatedAtBetween(startDate, endDate);
    }

    public Long countByStatus(ContactMessage.MessageStatus status) {
        return contactMessageRepository.countByStatus(status);
    }

    public List<ContactMessage> findByEmail(String email) {
        return contactMessageRepository.findByEmailOrderByCreatedAtDesc(email);
    }

    public List<ContactMessage> findByPhone(String phone) {
        return contactMessageRepository.findByPhoneOrderByCreatedAtDesc(phone);
    }

    @Transactional
    public void deleteMessage(Long id) {
        ContactMessage message = findById(id);

        // Decrement lawyer's case count if assigned
        if (message.getAssignedLawyer() != null) {
            lawyerService.decrementCaseCount(message.getAssignedLawyer().getId());
        }

        contactMessageRepository.deleteById(id);
        log.info("Deleted contact message with ID: {}", id);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null || xForwardedForHeader.isEmpty()) {
            return request.getRemoteAddr();
        } else {
            return xForwardedForHeader.split(",")[0].trim();
        }
    }
}
