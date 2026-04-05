# FinBaby (Jama)

A personal finance and expense tracker Android app designed for Indian middle-class users. Track expenses, set budgets, auto-import bank SMS transactions, and get smart saving tips — all in a calm, minimal interface.

## Features

- **SMS Auto-Import** — Reads bank SMS messages and auto-categorizes transactions
- **Manual Entry** — Add income/expense with category, notes, and date
- **50/30/20 Budgeting** — Budget planner splitting needs, wants, and savings
- **Reports & Charts** — Donut charts by category, daily bar charts, monthly trends
- **Smart Tips** — Personalized saving suggestions based on spending patterns
- **Search** — Full-text search across all transactions
- **CSV Export** — Export transactions to CSV
- **Backup & Restore** — JSON-based backup via Gson
- **Biometric Lock** — Fingerprint/face authentication
- **Daily Reminders** — WorkManager-powered notifications

## Tech Stack

- **Kotlin** + **Jetpack Compose** + **Material 3**
- **Room** — Local SQLite database
- **Hilt** — Dependency injection
- **Navigation Compose** — Single-activity navigation
- **Vico** — Charts library
- **WorkManager** — Background tasks (reminders, recurring transactions, budget alerts)
- **DataStore** — User preferences

## Build

```bash
cd android
./gradlew assembleDebug      # Debug APK
./gradlew assembleRelease    # Signed release APK (minified + shrunk)
```

**Requirements:** JDK 17, Android SDK 35, min SDK 26

## Project Structure

```
android/app/src/main/java/com/finbaby/app/
├── data/          # Room entities, DAOs, repositories
├── di/            # Hilt DI module
├── navigation/    # NavGraph and routes
├── sms/           # SMS reading, parsing, bank sender mapping
├── ui/            # Screens: home, reports, budget, settings, search, tips, onboarding
├── util/          # Date, currency, CSV, backup, tips engine
└── worker/        # WorkManager: reminders, recurring transactions, budget alerts
```

## Design

"The Mindful Ledger" — teal & amber palette, soft minimalism, no hard borders, tonal layering. See [DESIGN.md](stitch_output/stitch/jama_aura/DESIGN.md) for the full design system spec.
