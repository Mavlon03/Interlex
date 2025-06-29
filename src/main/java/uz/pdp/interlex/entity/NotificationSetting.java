package uz.pdp.interlex.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Table(name = "notification_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lawyer_id")
    private Long lawyerId;

    @Column(name = "email_notifications")
    @Builder.Default
    private Boolean emailNotifications = true;

    @Column(name = "sms_notifications")
    @Builder.Default
    private Boolean smsNotifications = false;

    @Column(name = "telegram_notifications")
    @Builder.Default
    private Boolean telegramNotifications = false;

    @Column(name = "telegram_chat_id", length = 100)
    private String telegramChatId;

    @Column(name = "notification_hours_start")
    @Builder.Default
    private LocalTime notificationHoursStart = LocalTime.of(9, 0);

    @Column(name = "notification_hours_end")
    @Builder.Default
    private LocalTime notificationHoursEnd = LocalTime.of(18, 0);

    @Column(name = "weekend_notifications")
    @Builder.Default
    private Boolean weekendNotifications = false;

    // Relationship
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lawyer_id", insertable = false, updatable = false)
    private Lawyer lawyer;
}
