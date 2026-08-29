# Jama

A personal finance and expense tracker Android app designed for Indian middle-class users. Track expenses, set budgets, and get smart saving tips — all in a calm, minimal interface.

> Previously released as "FinBaby" (`com.finbaby.app`). That listing was suspended by Google Play in April 2026 for repeated rejections tied to the prior SMS auto-import feature, which has since been removed. This is the rebranded, policy-compliant relaunch.

## Features

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
android/app/src/main/java/com/jama/expense/
├── data/          # Room entities, DAOs, repositories
├── di/            # Hilt DI module
├── navigation/    # NavGraph and routes
├── ui/            # Screens: home, reports, budget, settings, search, tips, onboarding
├── util/          # Date, currency, CSV, backup, tips engine
└── worker/        # WorkManager: reminders, recurring transactions, budget alerts
```

## Deployment

CI/CD: tag push (`git tag v1.0.1 && git push origin v1.0.1`) → GitHub Actions builds, signs, uploads to Internal track, then promotes to Production with 10% staged rollout. See [DEPLOYMENT.md](DEPLOYMENT.md) for one-time Play Console setup.

## Design

"The Mindful Ledger" — teal & amber palette, soft minimalism, no hard borders, tonal layering.

---

### 🤝 Work with me

I'm an **AI Consultant · Forward Deployed Engineer** — I embed with teams and ship AI to production: agents, MCP integrations, and LLM features, with evals proving they work.

**→ [rohitraj.tech/en/hire](https://rohitraj.tech/en/hire)**
