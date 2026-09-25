package com.bharat.expensetracker.dto;

/** Request body for POST /api/expenses. */
public class CreateExpenseRequest {

    private double amount;
    private String description;
    private String category;
    /** ISO date yyyy-MM-dd */
    private String date;

    public CreateExpenseRequest() {
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
