package uz.pdp.interlex.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.pdp.interlex.entity.ContactMessage;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {

    List<ContactMessage> findByStatusOrderByCreatedAtDesc(ContactMessage.MessageStatus status);

    Page<ContactMessage> findByStatusOrderByCreatedAtDesc(ContactMessage.MessageStatus status, Pageable pageable);

    List<ContactMessage> findByServiceTypeOrderByCreatedAtDesc(ContactMessage.ServiceType serviceType);

    List<ContactMessage> findByClientTypeOrderByCreatedAtDesc(ContactMessage.ClientType clientType);

    List<ContactMessage> findByAssignedLawyerIdOrderByCreatedAtDesc(Long lawyerId);

    @Query("SELECT cm FROM ContactMessage cm WHERE cm.createdAt BETWEEN :startDate AND :endDate ORDER BY cm.createdAt DESC")
    List<ContactMessage> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(cm) FROM ContactMessage cm WHERE cm.status = :status")
    Long countByStatus(@Param("status") ContactMessage.MessageStatus status);

    @Query("SELECT cm FROM ContactMessage cm WHERE cm.email = :email ORDER BY cm.createdAt DESC")
    List<ContactMessage> findByEmailOrderByCreatedAtDesc(@Param("email") String email);

    @Query("SELECT cm FROM ContactMessage cm WHERE cm.phone = :phone ORDER BY cm.createdAt DESC")
    List<ContactMessage> findByPhoneOrderByCreatedAtDesc(@Param("phone") String phone);

    @Query("SELECT cm FROM ContactMessage cm WHERE " +
            "LOWER(cm.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(cm.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(cm.subject) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(cm.message) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<ContactMessage> searchMessages(@Param("keyword") String keyword, Pageable pageable);
}
