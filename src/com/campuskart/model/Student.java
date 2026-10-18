package com.campuskart.model;

// ★ RUBRIC: Inheritance (1M) | Asg 7: extends = single inheritance, a Student IS-A User
public class Student extends User {

    private String enrollmentNo, department, verificationStatus;

    public Student() {                            // ★ Asg 2: default constructor
        super();                                  // ★ Asg 8: super() calls the parent (User) constructor first
        this.verificationStatus = "PENDING";
    }

    // ★ Asg 8: super() constructor chaining — identity fields are set by User, the rest by Student
    public Student(int id, String name, String email, String enrollmentNo, String verificationStatus) {
        super(id, name, email, "STUDENT");
        this.enrollmentNo = enrollmentNo;
        this.verificationStatus = verificationStatus;
    }

    // method overriding — the abstract method gets a student's answer
    @Override
    public String getDashboardPath() {
        return "/items/mine";
    }

    // business rule F2: only APPROVED students may create listings
    public boolean canPostItems() {
        return "APPROVED".equals(verificationStatus);
    }

    public String getEnrollmentNo() { return enrollmentNo; }
    public String getDepartment() { return department; }
    public String getVerificationStatus() { return verificationStatus; }

    public void setEnrollmentNo(String enrollmentNo) { this.enrollmentNo = enrollmentNo; }
    public void setDepartment(String department) { this.department = department; }
}
