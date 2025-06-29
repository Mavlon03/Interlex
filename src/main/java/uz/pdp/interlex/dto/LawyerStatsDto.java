package uz.pdp.interlex.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LawyerStatsDto {
    private Long lawyerId;
    private String lawyerName;
    private String email;
    private Long totalCases;
    private Long completedCases;
    private Double completionRate;
}