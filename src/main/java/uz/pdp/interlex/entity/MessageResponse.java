package uz.pdp.interlex.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "message_responses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id", nullable = false)
    private Long messageId;

    @Column(name = "lawyer_id", nullable = false)
    private Long lawyerId;

    @Column(name = "response_text", nullable = false, columnDefinition = "TEXT")
    private String responseText;

    @Column(name = "response_type", length = 50)
    @Builder.Default
    private String responseType = "EMAIL";

    @CreationTimestamp
    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Column(name = "is_auto_response")
    @Builder.Default
    private Boolean isAutoResponse = false;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", insertable = false, updatable = false)
    private ContactMessage contactMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lawyer_id", insertable = false, updatable = false)
    private Lawyer lawyer;
}
