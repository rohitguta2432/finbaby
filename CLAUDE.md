# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

FinBaby (branded "Jama") is an Android personal finance/expense tracker app targeting Indian middle-class users. Built with Kotlin, Jetpack Compose, and Material 3. The app reads bank SMS messages to auto-import transactions, supports manual entry, budgeting (50/30/20 rule), reports with charts, and CSV export.

## Build Commands

All commands run from the `android/` directory:

```bash
cd android
./gradlew assembleDebug          # Debug build
./gradlew assembleRelease        # Release build (signed, minified)
./gradlew test                   # Unit tests
./gradlew connectedAndroidTest   # Instrumentation tests
./gradlew kspDebugKotlin         # Run KSP annotation processing (Room, Hilt)
```

## Architecture

**Stack:** Kotlin · Jetpack Compose · Material 3 · Hilt DI · Room DB · Navigation Compose · Vico Charts · WorkManager

**Package structure** (`com.finbaby.app`):

- `data/db/entity/` — Room entities: Transaction, Category, Budget, Profile
- `data/db/dao/` — Room DAOs for each entity
- `data/repository/` — Repository layer over DAOs (Transaction, Category, Budget, Profile)
- `di/AppModule` — Hilt singleton module providing database and all DAOs
- `navigation/FinBabyNavGraph` — Single NavHost with all routes defined in `Routes` object
- `ui/<feature>/` — Each screen has its own ViewModel + Composable (home, reports, budget, settings, search, detail, onboarding, salary, tips)
- `ui/components/` — Shared composables (bottom nav, top bar, logo, category icon, budget progress bar)
- `ui/theme/` — Color, Type, Theme definitions following the "Mindful Ledger" design system
- `sms/` — SMS parsing pipeline: SmsReader → SmsRegexEngine → SmsTransactionParser → BankSenderMap
- `worker/` — WorkManager workers: DailyReminder, RecurringTransaction, BudgetAlert
- `util/` — DateUtils, CurrencyFormatter, CsvExporter, BackupManager, CategoryMatcher, TipsEngine

**Data flow:** UI (Composable) → ViewModel → Repository → DAO → Room (SQLite `finbaby_db`)

**DI pattern:** Single Hilt module (`AppModule`) provides the database singleton and all DAOs. Repositories are constructor-injected.

**Navigation:** Onboarding → SalarySetup → Home. Main screens (Home, Reports, Budget, Settings, Search) use bottom nav with `popUpTo(HOME)` + `saveState/restoreState`.

## Key Design Decisions

- Room database version 1 with `exportSchema = true` — schemas go to `app/schemas/`
- Default categories are seeded on first DB creation via `RoomDatabase.Callback`
- Categories have a `budgetType` field (needs/wants) for 50/30/20 budgeting
- SMS import uses regex patterns mapped per bank sender ID (`BankSenderMap`)
- Design system: "Mindful Ledger" — teal/amber palette, no hard borders, tonal layering (see `stitch_output/stitch/jama_aura/DESIGN.md`)
- Fonts: Plus Jakarta Sans (display/headlines) + Inter (body/labels) via Google Fonts
- Min SDK 26, Target SDK 35, Java 17
