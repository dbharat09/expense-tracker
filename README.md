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
- All data stays on the device — nothing is sent to any server.
