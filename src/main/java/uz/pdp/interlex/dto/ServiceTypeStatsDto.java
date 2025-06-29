package uz.pdp.interlex.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceTypeStatsDto {
    private String serviceType;
    private String displayName;
    private Long caseCount;
    private Double percentage;
}