package com.bharat.expensetracker.exception;

/** Thrown for invalid client input → 400. */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
