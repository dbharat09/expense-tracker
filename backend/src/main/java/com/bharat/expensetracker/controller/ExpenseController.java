package com.bharat.expensetracker.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bharat.expensetracker.dto.CreateExpenseRequest;
import com.bharat.expensetracker.model.Expense;
import com.bharat.expensetracker.service.ExpenseService;

/**
 * REST API under /api. All origins allowed so the static demo
 * (e.g. GitHub Pages) can call this backend.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ExpenseController {

    private final ExpenseService service;

    public ExpenseController(ExpenseService service) {
        this.service = service;
    }

    @PostMapping("/expenses")
    public ResponseEntity<Expense> create(@RequestBody CreateExpenseRequest req) {
        Expense created = service.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/expenses")
    public Map<String, Object> list(@RequestParam(defaultValue = "0") int weekOffset) {
        List<Expense> expenses = service.listForWeek(weekOffset);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("expenses", expenses);
        out.put("weekStart", service.weekStart(weekOffset).toString());
        out.put("weekEnd", service.weekEnd(weekOffset).toString());
        out.put("weekLabel", service.weekLabel(weekOffset));
        return out;
    }

    @DeleteMapping("/expenses/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    public Map<String, Object> summary(@RequestParam(defaultValue = "0") int weekOffset) {
        return service.summary(weekOffset);
    }
}
