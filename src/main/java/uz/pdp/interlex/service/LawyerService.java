package uz.pdp.interlex.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.interlex.dto.LawyerDto;
import uz.pdp.interlex.entity.ContactMessage;
import uz.pdp.interlex.entity.Lawyer;
import uz.pdp.interlex.exeption.ResourceNotFoundException;
import uz.pdp.interlex.repo.LawyerRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LawyerService {

    private final LawyerRepository lawyerRepository;

    public Lawyer createLawyer(LawyerDto dto) {
        log.info("Creating new lawyer: {} {}", dto.getFirstName(), dto.getLastName());

        Lawyer lawyer = Lawyer.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .specializations(dto.getSpecializations())
                .yearsOfExperience(dto.getYearsOfExperience())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .maxCasesPerDay(dto.getMaxCasesPerDay() != null ? dto.getMaxCasesPerDay() : 5)
                .currentCaseCount(0)
                .build();

        Lawyer savedLawyer = lawyerRepository.save(lawyer);
        log.info("Lawyer created successfully with ID: {}", savedLawyer.getId());
        return savedLawyer;
    }

    public Lawyer findById(Long id) {
        return lawyerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lawyer not found with id: " + id));
    }

    public List<Lawyer> findAllActive() {
        return lawyerRepository.findByIsActiveTrueOrderByFirstNameAsc();
    }

    public Optional<Lawyer> findByEmail(String email) {
        return lawyerRepository.findByEmailAndIsActiveTrue(email);
    }

    public List<Lawyer> findBySpecialization(ContactMessage.ServiceType serviceType) {
        return lawyerRepository.findBySpecializationAndIsActiveTrue(serviceType);
    }

    public List<Lawyer> findAvailableLawyers() {
        return lawyerRepository.findAvailableLawyers();
    }

    public Lawyer findBestAvailableLawyer(ContactMessage.ServiceType serviceType) {
        List<Lawyer> availableLawyers;

        if (serviceType != null) {
            availableLawyers = lawyerRepository.findAvailableLawyersBySpecialization(serviceType);
        } else {
            availableLawyers = lawyerRepository.findAvailableLawyers();
        }

        return availableLawyers.isEmpty() ? null : availableLawyers.get(0);
    }

    @Transactional
    public Lawyer updateLawyer(Long id, LawyerDto dto) {
        Lawyer lawyer = findById(id);

        lawyer.setFirstName(dto.getFirstName());
        lawyer.setLastName(dto.getLastName());
        lawyer.setEmail(dto.getEmail());
        lawyer.setPhone(dto.getPhone());
        lawyer.setSpecializations(dto.getSpecializations());
        lawyer.setYearsOfExperience(dto.getYearsOfExperience());
        lawyer.setIsActive(dto.getIsActive());
        lawyer.setMaxCasesPerDay(dto.getMaxCasesPerDay());

        Lawyer updatedLawyer = lawyerRepository.save(lawyer);
        log.info("Updated lawyer with ID: {}", id);
        return updatedLawyer;
    }

    @Transactional
    public void incrementCaseCount(Long lawyerId) {
        Lawyer lawyer = findById(lawyerId);
        lawyer.setCurrentCaseCount(lawyer.getCurrentCaseCount() + 1);
        lawyerRepository.save(lawyer);
        log.debug("Incremented case count for lawyer {}: {}", lawyerId, lawyer.getCurrentCaseCount());
    }

    @Transactional
    public void decrementCaseCount(Long lawyerId) {
        Lawyer lawyer = findById(lawyerId);
        int newCount = Math.max(0, lawyer.getCurrentCaseCount() - 1);
        lawyer.setCurrentCaseCount(newCount);
        lawyerRepository.save(lawyer);
        log.debug("Decremented case count for lawyer {}: {}", lawyerId, newCount);
    }

    @Transactional
    public void deactivateLawyer(Long id) {
        Lawyer lawyer = findById(id);
        lawyer.setIsActive(false);
        lawyerRepository.save(lawyer);
        log.info("Deactivated lawyer with ID: {}", id);
    }

    @Transactional
    public void activateLawyer(Long id) {
        Lawyer lawyer = findById(id);
        lawyer.setIsActive(true);
        lawyerRepository.save(lawyer);
        log.info("Activated lawyer with ID: {}", id);
    }

    public Long countActiveLawyers() {
        return lawyerRepository.countActiveLawyers();
    }

    @Transactional
    public void deleteLawyer(Long id) {
        Lawyer lawyer = findById(id);
        lawyerRepository.delete(lawyer);
        log.info("Deleted lawyer with ID: {}", id);
    }
}
