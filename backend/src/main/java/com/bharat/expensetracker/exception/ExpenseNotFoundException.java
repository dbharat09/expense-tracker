package com.bharat.expensetracker.exception;

/** Thrown when an expense id does not exist → 404. */
public class ExpenseNotFoundException extends RuntimeException {

    public ExpenseNotFoundException(String id) {
        super("Expense not found: " + id);
    }
}
