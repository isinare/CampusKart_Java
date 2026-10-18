package com.campuskart.exception;

// ★ RUBRIC: Exception Handling (1M) | Asg 16: USER-DEFINED exception — extends Exception (checked)
//            Asg 8  : super(message) chains to the Exception constructor
public class InvalidPriceException extends Exception {

    public InvalidPriceException(String message) {
        super(message);                           // ★ Asg 8: super()
    }
}
