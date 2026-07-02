package com.upc.edubridge.student.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardSummary {
    private long totalStudents;
    private double averageGrade;
    private long highRiskCount;
    private long mediumRiskCount;
    private long lowRiskCount;

    private long grade10Count;
    private long grade11Count;
    private long grade12Count;
}