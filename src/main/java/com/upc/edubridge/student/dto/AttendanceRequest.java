package com.upc.edubridge.student.dto;

import java.util.Map;

public class AttendanceRequest {
    private Map<Long, Boolean> records;

    public Map<Long, Boolean> getRecords() { return records; }
    public void setRecords(Map<Long, Boolean> records) { this.records = records; }
}