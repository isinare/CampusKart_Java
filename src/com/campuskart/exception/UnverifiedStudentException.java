package com.campuskart.exception;

// ★ RUBRIC: Exception Handling | Asg 16: user-defined exception for feature F2 —
//            a student whose verification is not APPROVED tries to post an item
public class UnverifiedStudentException extends Exception {

    public UnverifiedStudentException(String message) {
        super(message);
    }
}
