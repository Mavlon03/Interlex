package uz.pdp.interlex.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsDto {
    private Long totalClients;
    private Long totalLawyers;
    private Double avgExperience;
    private Long successfulCases;
    private Long casesLast24h;
    private Long pendingCases;
    private Double successRate;
}