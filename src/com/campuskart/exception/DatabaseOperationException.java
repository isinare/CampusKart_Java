package com.campuskart.exception;

// ★ RUBRIC: Exception Handling | Asg 16: user-defined exception that WRAPS every SQLException,
//            so the raw database error never reaches the screen (it is logged instead).
public class DatabaseOperationException extends Exception {

    public DatabaseOperationException(String message) {
        super(message);
    }

    // keeps the original SQLException as the "cause" for the server log
    public DatabaseOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
