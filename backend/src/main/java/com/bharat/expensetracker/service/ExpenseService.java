package com.bharat.expensetracker.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Service;

import com.bharat.expensetracker.dto.CreateExpenseRequest;
import com.bharat.expensetracker.exception.BadRequestException;
import com.bharat.expensetracker.exception.ExpenseNotFoundException;
import com.bharat.expensetracker.model.Expense;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;

/**
 * Expense business logic backed by the Firestore {@code expenses} collection.
 * Weeks run Monday → Sunday.
 */
@Service
public class ExpenseService {

    private static final String COLLECTION = "expenses";
    private static final DateTimeFormatter DAY_FMT =
            DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH);

    private final Firestore firestore;

    public ExpenseService(Firestore firestore) {
        this.firestore = firestore;
    }

    /** Monday of the week {@code weekOffset} away from the current week. */
    public LocalDate weekStart(int weekOffset) {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.minusDays(today.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue());
        return monday.plusWeeks(weekOffset);
    }

    public LocalDate weekEnd(int weekOffset) {
        return weekStart(weekOffset).plusDays(6);
    }

    public String weekLabel(int weekOffset) {
        LocalDate start = weekStart(weekOffset);
        LocalDate end = weekEnd(weekOffset);
        String range = start.format(DAY_FMT) + " – " + end.format(DAY_FMT);
        if (weekOffset == 0) return "This week · " + range;
        if (weekOffset == -1) return "Last week · " + range;
        if (weekOffset == 1) return "Next week · " + range;
        return range;
    }

    public List<Expense> listForWeek(int weekOffset) {
        String start = weekStart(weekOffset).toString();
        String end = weekEnd(weekOffset).toString();
        try {
            CollectionReference col = firestore.collection(COLLECTION);
            List<QueryDocumentSnapshot> docs = col
                    .whereGreaterThanOrEqualTo("date", start)
                    .whereLessThanOrEqualTo("date", end)
                    .orderBy("date", Query.Direction.DESCENDING)
                    .get()
                    .get()
                    .getDocuments();
            List<Expense> result = new ArrayList<>(docs.size());
            for (QueryDocumentSnapshot doc : docs) {
                Expense e = doc.toObject(Expense.class);
                e.setId(doc.getId());
                result.add(e);
            }
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Firestore query interrupted", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Firestore query failed", e.getCause());
        }
    }

    public Expense create(CreateExpenseRequest req) {
        validate(req);
        CollectionReference col = firestore.collection(COLLECTION);
        DocumentReference ref = col.document();
        Expense expense = new Expense(
                ref.getId(),
                round2(req.getAmount()),
                req.getDescription() == null ? "" : req.getDescription().trim(),
                req.getCategory().trim(),
                req.getDate(),
                System.currentTimeMillis());
        try {
            ref.set(expense).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Firestore write interrupted", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Firestore write failed", e.getCause());
        }
        return expense;
    }

    public void delete(String id) {
        try {
            DocumentReference ref = firestore.collection(COLLECTION).document(id);
            DocumentSnapshot snap = ref.get().get();
            if (!snap.exists()) {
                throw new ExpenseNotFoundException(id);
            }
            ref.delete().get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Firestore delete interrupted", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Firestore delete failed", e.getCause());
        }
    }

    /** Weekly summary: totals per category, sorted high → low. */
    public Map<String, Object> summary(int weekOffset) {
        List<Expense> expenses = listForWeek(weekOffset);
        double total = expenses.stream().mapToDouble(Expense::getAmount).sum();

        Map<String, double[]> byCat = new TreeMap<>(); // category -> [total, count]
        for (Expense e : expenses) {
            double[] acc = byCat.computeIfAbsent(e.getCategory(), k -> new double[2]);
            acc[0] += e.getAmount();
            acc[1] += 1;
        }

        List<Map<String, Object>> rows = byCat.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue()[0], a.getValue()[0]))
                .map(entry -> {
                    double catTotal = round2(entry.getValue()[0]);
                    long pct = total > 0 ? Math.round((entry.getValue()[0] / total) * 100) : 0;
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("category", entry.getKey());
                    row.put("total", catTotal);
                    row.put("count", (long) entry.getValue()[1]);
                    row.put("pct", pct);
                    return row;
                })
                .toList();

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("total", round2(total));
        out.put("byCategory", rows);
        out.put("topCategory", rows.isEmpty() ? null : rows.get(0).get("category"));
        out.put("weekStart", weekStart(weekOffset).toString());
        out.put("weekEnd", weekEnd(weekOffset).toString());
        out.put("weekLabel", weekLabel(weekOffset));
        return out;
    }

    private void validate(CreateExpenseRequest req) {
        if (req == null || req.getAmount() <= 0) {
            throw new BadRequestException("amount must be greater than 0");
        }
        if (req.getCategory() == null || req.getCategory().isBlank()) {
            throw new BadRequestException("category is required");
        }
        if (req.getDate() == null || req.getDate().isBlank()) {
            throw new BadRequestException("date is required");
        }
        try {
            LocalDate.parse(req.getDate());
        } catch (DateTimeParseException e) {
            throw new BadRequestException("date must be ISO yyyy-MM-dd");
        }
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
