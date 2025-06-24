package uz.pdp.interlex.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.interlex.entity.ContactMessage;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LawyerDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Set<ContactMessage.ServiceType> specializations;
    private Integer yearsOfExperience;
    private Boolean isActive;
    private Integer maxCasesPerDay;
    private Integer currentCaseCount;
}
