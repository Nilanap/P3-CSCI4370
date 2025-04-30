package edu.uga.csci4370.group12.project3.Models;



public class Enrollment {
    private final String userId;
    private final String classId;
    private final String enrollmentDate;

    public Enrollment(String userId, String classId, String enrollmentDate) {
        this.userId = userId;
        this.classId = classId;
        this.enrollmentDate = enrollmentDate;
    }

    public String getUserId() { return userId; }
    public String getClassId() { return classId; }
    public String getEnrollmentDate() { return enrollmentDate; }
}