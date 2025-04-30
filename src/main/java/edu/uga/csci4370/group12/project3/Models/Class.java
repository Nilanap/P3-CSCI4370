package edu.uga.csci4370.group12.project3.Models;



public class Class {
    private final String classId;
    private final String classCode;
    private final String className;

    public Class(String classId, String classCode, String className) {
        this.classId = classId;
        this.classCode = classCode;
        this.className = className;
    }

    public String getClassId() { return classId; }
    public String getClassCode() { return classCode; }
    public String getClassName() { return className; }
}
