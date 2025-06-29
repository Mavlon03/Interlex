package uz.pdp.interlex.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.pdp.interlex.entity.ContactMessage;
import uz.pdp.interlex.entity.Lawyer;

import java.util.List;
import java.util.Optional;

@Repository
public interface LawyerRepository extends JpaRepository<Lawyer, Long> {

    List<Lawyer> findByIsActiveTrueOrderByFirstNameAsc();

    Optional<Lawyer> findByEmailAndIsActiveTrue(String email);

    @Query("SELECT l FROM Lawyer l WHERE l.isActive = true AND :serviceType MEMBER OF l.specializations")
    List<Lawyer> findBySpecializationAndIsActiveTrue(@Param("serviceType") ContactMessage.ServiceType serviceType);

    @Query("SELECT l FROM Lawyer l WHERE l.isActive = true AND l.currentCaseCount < l.maxCasesPerDay")
    List<Lawyer> findAvailableLawyers();

    @Query("SELECT l FROM Lawyer l WHERE l.isActive = true AND :serviceType MEMBER OF l.specializations AND l.currentCaseCount < l.maxCasesPerDay ORDER BY l.currentCaseCount ASC")
    List<Lawyer> findAvailableLawyersBySpecialization(@Param("serviceType") ContactMessage.ServiceType serviceType);

    @Query("SELECT COUNT(l) FROM Lawyer l WHERE l.isActive = true")
    Long countActiveLawyers();

    Optional<Lawyer> findByEmail(String username);

    @Query("SELECT AVG(l.yearsOfExperience) FROM Lawyer l WHERE l.isActive = true")
    Double findAverageExperience();

    @Query("SELECT l.id, l.firstName, l.lastName, l.email, " +
            "COUNT(cm) as totalCases, " +
            "SUM(CASE WHEN cm.status = uz.pdp.interlex.entity.ContactMessage.MessageStatus.CLOSED THEN 1 ELSE 0 END) as completedCases " +
            "FROM Lawyer l " +
            "LEFT JOIN ContactMessage cm ON cm.assignedLawyer.id = l.id " +
            "WHERE l.isActive = true " +
            "GROUP BY l.id " +
            "ORDER BY completedCases DESC")
    List<Object[]> getLawyerStats();


}
