package com.example.incidents.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsResponse {
    private long totalUsers;
    private long employees;
    private long technicians;
    private long totalIncidents;
    private long openIncidents;
    private long inProgressIncidents;
    private long closedIncidents;
}
