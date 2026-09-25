# Expense Tracker — Backend (Spring Boot + Firestore)

Java 17 · Spring Boot 3.2.x · Maven · Firebase Admin SDK 9.2.0.
Serves the REST API under `/api` and the frontend from `src/main/resources/static/`.

## Environment variables

| Variable | Required | What it is |
|---|---|---|
| `FIREBASE_SERVICE_ACCOUNT` | one of the three | Full service-account JSON (paste the whole file contents). Highest priority. |
| `GOOGLE_APPLICATION_CREDENTIALS` | one of the three | Path to the service-account JSON file on disk. |
| `FIRESTORE_EMULATOR_HOST` | one of the three | e.g. `localhost:8080` — local Firestore emulator, no real credentials needed. |
| `FIREBASE_PROJECT_ID` | optional | Project id (auto-detected from the service-account JSON when present). |
| `PORT` | optional | HTTP port, defaults to `8080`. |

The app fails fast at startup with a clear error if none of the three credential
options is set. `GET /api/health` never touches Firestore.

## Firestore setup (free tier)

1. Go to the [Firebase console](https://console.firebase.google.com/) → create a project (or reuse one).
2. **Build → Firestore Database** → Create database → start in production mode, pick a region.
3. **Project settings → Service accounts** → Generate new private key → downloads a JSON file.
   - Locally: point `GOOGLE_APPLICATION_CREDENTIALS` at that file, or paste its contents into `FIREBASE_SERVICE_ACCOUNT`.
   - The free Spark plan covers this usage comfortably.
4. No schema or indexes to create — the app uses a single `expenses` collection with a single-field range query on `date` (no composite index needed).

## Run locally

```bash
cd backend
export FIRESTORE_EMULATOR_HOST=localhost:8080   # or set real credentials instead
mvn spring-boot:run
# API at http://localhost:8080/api/health
# UI at  http://localhost:8080/
```

With the emulator running, `mvn spring-boot:run` boots without any cloud credentials.

## API

| Method | Path | Notes |
|---|---|---|
| GET | `/api/health` | `{"status":"ok"}` — no Firestore call |
| POST | `/api/expenses` | body `{amount, description, category, date}` → 201, validates input |
| GET | `/api/expenses?weekOffset=0` | week's expenses + `weekStart`/`weekEnd`/`weekLabel` |
| DELETE | `/api/expenses/{id}` | 204, or 404 `{"error": ...}` |
| GET | `/api/summary?weekOffset=0` | totals per category, sorted desc |

`weekOffset`: `0` = current week (Mon–Sun), `-1` = last week, `1` = next week.
Errors always come back as `{"error": "..."}`. CORS allows all origins.

## Deploy on Render (free)

1. Push this repo to GitHub (the `backend/` folder is the deploy root).
2. Render dashboard → New → Web Service → point at the repo.
   - **Root Directory:** `backend`
   - **Build Command:** `mvn -DskipTests package` (or use the Dockerfile: set Runtime to Docker)
   - **Start Command:** `java -jar target/expense-tracker-1.0.0.jar`
   - If using Docker: Render detects `Dockerfile` automatically when Runtime = Docker.
3. Environment → add `FIREBASE_SERVICE_ACCOUNT` with the full service-account JSON.
4. Deploy — note the `https://<your-app>.onrender.com` URL and set the frontend to call it
   (the bundled UI auto-detects a same-origin backend; point GitHub Pages at it via a
   small `API_BASE` tweak if you split hosting).
