package uz.pdp.interlex.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.interlex.entity.MessageResponse;

import java.util.List;

@Repository
public interface MessageResponseRepository extends JpaRepository<MessageResponse, Long> {

    List<MessageResponse> findByMessageIdOrderBySentAtDesc(Long messageId);

    List<MessageResponse> findByLawyerIdOrderBySentAtDesc(Long lawyerId);

    List<MessageResponse> findByIsAutoResponseOrderBySentAtDesc(Boolean isAutoResponse);
}
