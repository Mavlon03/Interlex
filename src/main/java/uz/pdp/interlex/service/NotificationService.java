package uz.pdp.interlex.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.pdp.interlex.entity.ContactMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailService emailService;

    public void sendNewMessageNotifications(ContactMessage message) {
        try {
            // Send notification to admin
            emailService.sendNewMessageNotificationToAdmin(message);

            // If lawyer is assigned, send notification to lawyer
            if (message.getAssignedLawyer() != null) {
                emailService.sendLawyerAssignmentNotification(message, message.getAssignedLawyer());
            }

            log.info("New message notifications sent for message ID: {}", message.getId());
        } catch (Exception e) {
            log.error("Failed to send new message notifications for message ID: {}", message.getId(), e);
        }
    }

    public void sendAssignmentNotification(ContactMessage message) {
        try {
            if (message.getAssignedLawyer() != null) {
                emailService.sendLawyerAssignmentNotification(message, message.getAssignedLawyer());
                log.info("Assignment notification sent for message ID: {} to lawyer ID: {}",
                        message.getId(), message.getAssignedLawyer().getId());
            }
        } catch (Exception e) {
            log.error("Failed to send assignment notification for message ID: {}", message.getId(), e);
        }
    }

    public void sendStatusUpdateNotification(ContactMessage message, ContactMessage.MessageStatus oldStatus) {
        try {
            // Only send notification for certain status changes
            if (shouldNotifyStatusChange(oldStatus, message.getStatus())) {
                emailService.sendStatusUpdateNotification(message);
                log.info("Status update notification sent for message ID: {} ({}->{})",
                        message.getId(), oldStatus, message.getStatus());
            }
        } catch (Exception e) {
            log.error("Failed to send status update notification for message ID: {}", message.getId(), e);
        }
    }

    private boolean shouldNotifyStatusChange(ContactMessage.MessageStatus oldStatus, ContactMessage.MessageStatus newStatus) {
        // Send notification when status changes to RESPONDED or CLOSED
        return newStatus == ContactMessage.MessageStatus.RESPONDED ||
                newStatus == ContactMessage.MessageStatus.CLOSED;
    }
}
