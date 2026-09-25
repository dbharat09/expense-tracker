package com.bharat.expensetracker.model;

/**
 * An expense stored in the Firestore {@code expenses} collection.
 * {@code id} is the Firestore document id.
 */
public class Expense {

    private String id;
    private double amount;
    private String description;
    private String category;
    /** ISO date yyyy-MM-dd */
    private String date;
    /** epoch millis when the record was created */
    private long createdAt;

    public Expense() {
    }

    public Expense(String id, double amount, String description, String category, String date, long createdAt) {
        this.id = id;
        this.amount = amount;
        this.description = description;
        this.category = category;
        this.date = date;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
