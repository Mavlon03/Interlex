package uz.pdp.interlex.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "contact_messages")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type")
    private ServiceType serviceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "client_type")
    private ClientType clientType;

    @Column(nullable = false, length = 200)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "lang_code", length = 5)
    private String langCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MessageStatus status = MessageStatus.NEW;

    @Column(name = "client_ip", length = 45)
    private String clientIp;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    private Lawyer assignedLawyer;

    // Enums
    @Getter
    public enum ServiceType {
        CORPORATE("Корпоратив ҳуқуқ"),
        CIVIL("Фуқаролик ҳуқуқи"),
        CRIMINAL("Жиноят ҳуқуқи"),
        FAMILY("Оила ҳуқуқи"),
        BUSINESS("Бизнес ҳуқуқи"),
        TAX("Солиқ ҳуқуқи"),
        INTERNATIONAL("Халқаро ҳуқуқ");

        private final String displayName;

        ServiceType(String displayName) {
            this.displayName = displayName;
        }

    }

    @Getter
    public enum ClientType {
        INDIVIDUAL("Жисмоний шахс"),
        LEGAL("Юридик шахс");

        private final String displayName;

        ClientType(String displayName) {
            this.displayName = displayName;
        }

    }

    @Getter
    public enum MessageStatus {
        NEW("Янги"),
        IN_PROGRESS("Кўриб чиқилмоқда"),
        RESPONDED("Жавоб берилган"),
        CLOSED("Ёпилган");

        private final String displayName;

        MessageStatus(String displayName) {
            this.displayName = displayName;
        }

    }
}
