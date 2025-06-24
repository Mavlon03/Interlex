package uz.pdp.interlex.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.interlex.entity.NotificationSetting;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {

    Optional<NotificationSetting> findByLawyerId(Long lawyerId);

    List<NotificationSetting> findByEmailNotificationsTrue();

    List<NotificationSetting> findBySmsNotificationsTrue();

    List<NotificationSetting> findByTelegramNotificationsTrue();
}
