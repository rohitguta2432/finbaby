# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Jama (package `com.jama.expense`) is an Android personal finance/expense tracker targeting Indian middle-class users. Built with Kotlin, Jetpack Compose, and Material 3. Manual entry only — no SMS reading. Supports budgeting (50/30/20 rule), reports with charts, CSV export, biometric lock, and daily reminders.

> Previously released as "FinBaby" (`com.finbaby.app`); that listing was suspended by Google Play in April 2026 for repeated rejections tied to the prior SMS auto-import feature. The SMS package was removed and the app was relaunched under a new package name per Google's stated remedy.

## Build Commands

All commands run from the `android/` directory:

```bash
cd android
./gradlew assembleDebug          # Debug build
./gradlew assembleRelease        # Release build (signed, minified)
./gradlew bundleRelease          # Signed release AAB (for Play Store)
./gradlew test                   # Unit tests
./gradlew connectedAndroidTest   # Instrumentation tests
./gradlew kspDebugKotlin         # Run KSP annotation processing (Room, Hilt)
```

Release signing reads from env vars (`FINBABY_STORE_FILE`, `FINBABY_STORE_PASSWORD`, `FINBABY_KEY_ALIAS`, `FINBABY_KEY_PASSWORD`) or `android/keystore.properties` (gitignored).

## Architecture

**Stack:** Kotlin · Jetpack Compose · Material 3 · Hilt DI · Room DB · Navigation Compose · Vico Charts · WorkManager

**Package structure** (`com.jama.expense`):

- `data/db/entity/` — Room entities: Transaction, Category, Budget, Profile
- `data/db/dao/` — Room DAOs for each entity
- `data/repository/` — Repository layer over DAOs (Transaction, Category, Budget, Profile)
- `di/AppModule` — Hilt singleton module providing database and all DAOs
- `navigation/FinBabyNavGraph` — Single NavHost with all routes defined in `Routes` object
- `ui/<feature>/` — Each screen has its own ViewModel + Composable (home, reports, budget, settings, search, detail, onboarding, salary, tips)
- `ui/components/` — Shared composables (bottom nav, top bar, logo, category icon, budget progress bar)
- `ui/theme/` — Color, Type, Theme definitions following the "Mindful Ledger" design system
- `worker/` — WorkManager workers: DailyReminder, RecurringTransaction, BudgetAlert
- `util/` — DateUtils, CurrencyFormatter, CsvExporter, BackupManager, CategoryMatcher, TipsEngine

**Data flow:** UI (Composable) → ViewModel → Repository → DAO → Room (SQLite `finbaby_db`)

**DI pattern:** Single Hilt module (`AppModule`) provides the database singleton and all DAOs. Repositories are constructor-injected.

**Navigation:** Onboarding → SalarySetup → Home. Main screens (Home, Reports, Budget, Settings, Search) use bottom nav with `popUpTo(HOME)` + `saveState/restoreState`.

## Key Design Decisions

- Room database version 1 with `exportSchema = true` — schemas go to `app/schemas/`
- Default categories are seeded on first DB creation via `RoomDatabase.Callback`
- Categories have a `budgetType` field (needs/wants) for 50/30/20 budgeting
- Design system: "Mindful Ledger" — teal/amber palette, no hard borders, tonal layering
- Fonts: Plus Jakarta Sans (display/headlines) + Inter (body/labels) via Google Fonts
- Min SDK 26, Target SDK 35, Java 17

## Naming nuances (legacy)

- Class names still use `FinBaby*` (FinBabyApp, FinBabyDatabase, FinBabyNavGraph) — internal only, not user-visible.
- Display name (`android:label` in manifest) is "Jama".
- Database name (`finbaby_db`) kept for migration continuity if anyone updated from the old app.
- Env var prefix is `FINBABY_*` for signing (kept across the rename to avoid breaking GitHub secrets).
