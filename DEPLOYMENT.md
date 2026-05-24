# Jama — Play Store deployment

CI/CD: tag push → GitHub Actions builds the AAB, signs it, uploads to **Internal** track, then promotes to **Production** with a 10% staged rollout.

```
git tag v1.0.1 && git push origin v1.0.1
```

That's the steady-state flow. Everything below is one-time setup (or one-time-per-environment-change).

> **Context: this is a relaunch.** The previous package `com.finbaby.app` was suspended by Google Play in April 2026 ("Repeated app rejections" enforcement). The SMS auto-import feature has been removed and the app rebranded as Jama under `com.jama.expense` per Google's stated remedy.

---

## One-time setup

### 1. Local machine

Your release keystore lives at `~/.config/finbaby/finbaby-release.jks` (PKCS12, RSA 2048, 25-year validity). Credentials are in `~/.config/finbaby/keystore-credentials.txt` — **move them into your password manager and delete the txt file.**

Keystore SHA-256 fingerprint:
```
F4:68:BF:16:80:E7:18:35:22:85:C1:47:78:90:72:3A:8A:24:53:DD:9A:07:BA:3D:69:C8:E9:C0:0A:AF:A8:46
```

To build a signed release locally, create `android/keystore.properties` from the example:
```bash
cp android/keystore.properties.example android/keystore.properties
# fill in real values; this file is gitignored
```
Then `cd android && ./gradlew bundleRelease`.

### 2. Google Play Console

The old `com.finbaby.app` listing is suspended permanently — do not try to revive it. You need to **create a new app listing** under `com.jama.expense`.

**a. Create the new app.** Play Console → All apps → **Create app**.
- App name: `Jama` (or `Jama — Expense Tracker`)
- Default language: English (United States)
- App or game: App
- Free or paid: Free
- Confirm both declarations.

**b. Complete the required "Set up your app" sections** before you can publish anything:
- App access (no login screen needed → "All functionality is available without restrictions")
- Ads ("No, my app does not contain ads")
- Content rating questionnaire
- Target audience and content (18+; not directed at children)
- News app (No)
- COVID-19 contact tracing (No)
- Data safety form — declare: stores Transaction data, Personal info (none collected), no data shared/transmitted off-device. Be precise; this is what tripped the previous review.
- Government apps (No)
- Financial features — **important.** Jama tracks personal finance; declare as a Personal Finance app. Do NOT mark as a regulated financial product.
- Privacy policy URL — `https://rohitguta2432.github.io/finbaby/privacy-policy.html` (or wherever you host it; GitHub Pages on this repo serves it for free).
- Store listing (title, short + full description, screenshots, feature graphic, icon — at minimum)

**c. Service account for API access.** Needed so GitHub Actions can publish on your behalf.

1. Play Console → **Setup → API access** → link a Google Cloud project (or create one).
2. Under "Service accounts", click **Create new service account** → opens GCP IAM. Create a service account, then create a JSON key for it and download it.
3. Back in Play Console, click **Grant access** next to the service account. Permissions:
   - **Releases** → Release manager (or Admin if you want metadata edits too)
   - Restrict to the Jama app only.

Save the JSON key as `~/.config/finbaby/play-key.json` (gitignored locally) — you'll also base64-encode it for the GitHub secret below.

**d. First-time release upload.** Play API can only update *existing* tracks. Upload the AAB manually once to create the Internal track:
```bash
cd android
FINBABY_STORE_FILE=~/.config/finbaby/finbaby-release.jks \
FINBABY_STORE_PASSWORD='<password>' \
FINBABY_KEY_ALIAS=finbaby \
FINBABY_KEY_PASSWORD='<password>' \
./gradlew bundleRelease
```
Upload `app/build/outputs/bundle/release/app-release.aab` to **Testing → Internal testing → Create new release**, save as draft. After that, CI takes over.

### 3. GitHub Secrets

In `rohitguta2432/finbaby` → Settings → Secrets and variables → Actions. The first 4 are already set; only `FINBABY_PLAY_JSON_B64` is missing.

| Secret | How to compute | Status |
|---|---|---|
| `FINBABY_KEYSTORE_B64` | `base64 -i ~/.config/finbaby/finbaby-release.jks \| pbcopy` | ✅ set |
| `FINBABY_STORE_PASSWORD` | from credentials file | ✅ set |
| `FINBABY_KEY_ALIAS` | `finbaby` | ✅ set |
| `FINBABY_KEY_PASSWORD` | same as store password (PKCS12) | ✅ set |
| `FINBABY_PLAY_JSON_B64` | `base64 -i ~/.config/finbaby/play-key.json \| pbcopy` | ✅ set |

---

## What's complete and what's left in Play Console

As of 2026-05-24, the Jama app listing is wired up except for **5 image uploads** that need to happen in the browser (Play Console's Angular uploader resists programmatic injection from automation tools).

**✅ Complete (10/11 + descriptions):**
- App created with package `com.jama.expense`
- Privacy policy URL, App access, Ads, Content rating (All ages),
  Target audience (18+), Data safety ("no data collected"),
  Government apps, Financial features, Health, App category (Finance),
  Contact details, Store listing title + short + full description
- CI workflow has already drafted release **0.0.2** to Internal track

**❌ Last step — drag-and-drop 5 PNG files in browser:**

1. Open https://play.google.com/console/u/0/developers/7974256350151127084/app/4972206024482393782/main-store-listing
2. Scroll to **Graphics** section
3. Drag each file from `~/Documents/finbaby/store-assets/` (or `~/Downloads/`) onto the matching upload zone:

   | Zone | File |
   |---|---|
   | App icon | `icon-512.png` |
   | Feature graphic | `feature-1024x500.png` |
   | Phone screenshots (need at least 2) | `screenshot-1-home.png`, `screenshot-2-budget.png`, `screenshot-3-reports.png` |

4. Click **Save** at the bottom right.

> **Note on screenshots:** these are programmatically generated placeholders with the right dimensions and branding, so they'll pass technical Play review. Replace them with real screenshots from a running build before you market the app to actual users. The `store-assets/generate_assets.py` script can re-generate them if you tweak the design.

## Promote the existing draft to public Internal testing

The CI run drafted release **0.0.2** to Internal but didn't roll it out. To make it installable:

1. Play Console → Jama → **Testing → Internal testing → Testers** tab → add testers (your email at minimum) or create an email list.
2. Back on the release → **Roll out to internal testing** button.

You'll then get an opt-in URL you can visit on your phone to install Jama from the Play Store.

## Cutting a production release

```bash
# bump versionName in commits / changelog as desired
git tag v1.0.0
git push origin v1.0.0
```

Watch it in **Actions** tab. The workflow:
1. Builds a signed `.aab` (versionCode = GH run number, versionName from tag).
2. Uploads as a **draft** to the Internal track.
3. Promotes the same build to **Production** with a 10% staged rollout.

To bump the rollout fraction later, do it from Play Console → Production → Manage rollout.

### Manual run (override track / rollout)

Actions tab → **Android Release** → Run workflow. Pick track, toggle promote, set rollout %.

### Rollback

You can't truly "rollback" once users have updated — but you can:
- **Halt the staged rollout** from Play Console (stops any further users from getting the bad build).
- Cut a new tag with the fix and ship it the same way.

---

## Pre-flight reminders for Play review

The previous package was suspended for "Repeated app rejections" tied to SMS handling. To not repeat that:

- **No restricted permissions.** The current manifest only requests `POST_NOTIFICATIONS`, `USE_BIOMETRIC`, `RECEIVE_BOOT_COMPLETED` — all standard. Don't add `READ_SMS` / `RECEIVE_SMS` / `CALL_LOG` etc. without an approved Permissions Declaration.
- **Data safety form must match the code.** Declare exactly what the app processes; nothing is sent off-device.
- **Target API level** is 35 ✓ (Google requires 34+ as of Aug 2025).
- **Privacy policy URL** must be reachable from the listing. Enable GitHub Pages on this repo (Settings → Pages → from main branch root) to host `privacy-policy.html`.
- **App name "Jama"** is intentionally different from the suspended "FinBaby" listing — required by Google's relaunch guidance.

---

## File map

| Path | Purpose |
|---|---|
| `android/app/build.gradle.kts` | reads signing from env vars or `keystore.properties` |
| `android/keystore.properties.example` | template for local signing |
| `android/Gemfile` | locks Fastlane version |
| `android/fastlane/Appfile` | Play package (`com.jama.expense`) + service account path |
| `android/fastlane/Fastfile` | `build` / `internal` / `promote_to_production` / `release` lanes |
| `.github/workflows/release.yml` | tag-triggered CI pipeline |
| `~/.config/finbaby/` | **local, never commit** — keystore + service account key + creds (folder name kept across rename) |
