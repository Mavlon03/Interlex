package uz.pdp.interlex.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "lawyers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lawyer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @ElementCollection(targetClass = ContactMessage.ServiceType.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "lawyer_specializations", joinColumns = @JoinColumn(name = "lawyer_id"))
    @Column(name = "specialization")
    private Set<ContactMessage.ServiceType> specializations;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "max_cases_per_day")
    @Builder.Default
    private Integer maxCasesPerDay = 5;

    @Column(name = "current_case_count")
    @Builder.Default
    private Integer currentCaseCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public String getPassword() {
        return "123";
    }

    public enum LawyerStatus {

    }

}
