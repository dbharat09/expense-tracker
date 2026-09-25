# Expense Tracker

A simple, clean single-page web app to track your spending and see where your money goes each week.

## What it does

- **Add Expense** — record an expense with amount, description, category (Food, Transport, Shopping, Bills, Entertainment, Other), and date
- **Weekly Summary** — see total spent for the week plus a per-category breakdown, sorted highest to lowest, with percentage bars so it's obvious where most of your money is going
- **This week's list** — view all expenses for the selected week with a delete option
- **Week navigation** — move between previous / current / next weeks on both screens
- Expenses are saved in the browser's `localStorage`, so they persist between visits (per device)

## How to run

No build step needed — it's plain HTML/CSS/JS.

Option 1 — open directly:
```
open ~/workspace/expense-tracker/index.html   # macOS
# or just double-click index.html in your file manager
```

Option 2 — serve statically (recommended, avoids any file:// quirks):
```
cd ~/workspace/expense-tracker
python3 -m http.server 8080
# then open http://localhost:8080
```

## Files

- `index.html` — page structure: two screens (Add Expense / Weekly Summary) with tab navigation
- `styles.css` — clean, modern, mobile-friendly styling
- `app.js` — all logic: form validation, localStorage persistence, Monday→Sunday week filtering, category totals, percentage bars
- `README.md` — this file

## Notes

- Weeks run Monday → Sunday.
- Form validation: amount must be greater than 0, a category must be selected, and a date is required.
- All data stays on the device — nothing is sent to any server (in v1 mode).

---

## v2: Full-stack (Spring Boot + Firestore)

The `backend/` directory adds a real backend so expenses sync across devices:

- **Stack:** Java 17, Spring Boot 3.2.x, Maven, Firebase Admin SDK 9.2.0, Firestore (free tier).
- **Layout:**
  - `backend/pom.xml` — Maven build (includes `spring-boot-maven-plugin`, so `mvn spring-boot:run` works).
  - `backend/src/main/java/com/bharat/expensetracker/` — application code:
    - `ExpenseTrackerApplication.java` — boot entry point
    - `config/FirebaseConfig.java` — Firestore client; credentials from `FIREBASE_SERVICE_ACCOUNT` (full JSON) → `GOOGLE_APPLICATION_CREDENTIALS` (file path) → `FIRESTORE_EMULATOR_HOST` (local emulator); fails fast at startup if none is set
    - `model/Expense.java` — `id, amount, description, category, date (yyyy-MM-dd), createdAt`
    - `dto/CreateExpenseRequest.java` — POST body
    - `service/ExpenseService.java` — Firestore queries (collection `expenses`, ordered by `date`), week math, summary aggregation
    - `controller/ExpenseController.java` — REST API, CORS open to all origins
    - `exception/` — `BadRequestException`, `ExpenseNotFoundException`, `GlobalExceptionHandler` (all errors as `{"error": "..."}`)
  - `backend/src/main/resources/static/` — the same frontend (served at `/` when running the jar)
  - `backend/src/main/resources/application.properties` — `server.port=${PORT:8080}`
  - `backend/Dockerfile` — multi-stage build → `java -jar`, respects `$PORT`
  - `backend/README.md` — env vars, Firestore setup, local run, Render deploy notes

### API

| Method | Path | Notes |
|---|---|---|
| GET | `/api/health` | `{"status":"ok"}` — never touches Firestore |
| POST | `/api/expenses` | `{amount, description, category, date}` → 201; 400 on bad input |
| GET | `/api/expenses?weekOffset=0` | week's expenses + `weekStart` / `weekEnd` / `weekLabel` |
| DELETE | `/api/expenses/{id}` | 204, or 404 |
| GET | `/api/summary?weekOffset=0` | `{total, byCategory:[{category,total,count,pct}], topCategory, ...}` sorted desc |

`weekOffset`: `0` = current week (Mon–Sun), `-1` = last week, `1` = next week.

### How to run

```bash
cd backend
export FIRESTORE_EMULATOR_HOST=localhost:8080   # or set real Firebase credentials
mvn spring-boot:run
# UI + API at http://localhost:8080  (try /api/health)
```

The frontend auto-detects the backend: if `GET /api/health` (same origin) succeeds it uses
the API for everything; otherwise it falls back to the original `localStorage` behavior —
so the GitHub Pages demo keeps working untouched.
