# FinBaby — Play Store deployment

CI/CD: tag push → GitHub Actions builds the AAB, signs it, uploads to **Internal** track, then promotes to **Production** with a 10% staged rollout.

```
git tag v1.0.1 && git push origin v1.0.1
```

That's the steady-state flow. Everything below is one-time setup (or one-time-per-environment-change).

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

The app `com.finbaby.app` already exists in Play Console (rejected upload). Two things to do:

**a. Upload key may need reset.** If the rejected upload was accepted by Play (even if the release was rejected in review), Play has locked the upload-key fingerprint. The new keystore above will be rejected at upload time with `Your APK or Android App Bundle was signed with the wrong key`.

To check: Play Console → your app → **Setup → App integrity → App signing**. Compare the listed "Upload key certificate" SHA-256 with the fingerprint above.
- **Match** → nothing to do.
- **Mismatch** → click *Request upload key reset*, upload the new `.jks`, wait for Google to process (usually <48h).

**b. Service account for API access.** Needed so GitHub Actions can publish on your behalf.

1. Play Console → **Setup → API access** → link a Google Cloud project (or create one).
2. Under "Service accounts", click **Create new service account** → opens GCP IAM. Create a service account, then create a JSON key for it and download it.
3. Back in Play Console, click **Grant access** next to the service account. Permissions:
   - **Releases** → Release manager (or Admin if you want metadata edits too)
   - Restrict to the FinBaby app only.

Save the JSON key as `~/.config/finbaby/play-key.json` (gitignored locally) — you'll also base64-encode it for the GitHub secret below.

**c. First-time release upload.** Play API can only update *existing* tracks. Upload the AAB manually once to create the Internal track:
```bash
cd android
FINBABY_STORE_FILE=~/.config/finbaby/finbaby-release.jks \
FINBABY_STORE_PASSWORD='<password>' \
FINBABY_KEY_ALIAS=finbaby \
FINBABY_KEY_PASSWORD='<password>' \
./gradlew bundleRelease
```
Upload `app/build/outputs/bundle/release/app-release.aab` to Internal testing in Play Console, save as draft. After that, CI takes over.

### 3. GitHub Secrets

In `rohitguta2432/finbaby` → Settings → Secrets and variables → Actions, add:

| Secret | How to compute |
|---|---|
| `FINBABY_KEYSTORE_B64` | `base64 -i ~/.config/finbaby/finbaby-release.jks \| pbcopy` |
| `FINBABY_STORE_PASSWORD` | from credentials file |
| `FINBABY_KEY_ALIAS` | `finbaby` |
| `FINBABY_KEY_PASSWORD` | same as store password (PKCS12) |
| `FINBABY_PLAY_JSON_B64` | `base64 -i ~/.config/finbaby/play-key.json \| pbcopy` |

---

## Cutting a release

```bash
# bump versionName in commits / changelog as desired
git tag v1.0.1
git push origin v1.0.1
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

The previous submission was rejected. A few things worth verifying *before* the next promote_to_production:

- **SMS permissions** (`READ_SMS`, `RECEIVE_SMS`) fall under Google's [restricted permissions policy](https://support.google.com/googleplay/android-developer/answer/9047303). Apps that aren't the default SMS handler must submit a **Permissions Declaration form** with justification + a demo video. Without that, the app will be rejected again regardless of the privacy policy fix. The cleanest alternative is to use the [SmsRetriever API](https://developers.google.com/identity/sms-retriever) (no permission needed) or make the user paste/forward SMS manually. Worth deciding before next submit.
- **Data safety form** in Play Console must declare every type of data the app collects/processes — SMS content is sensitive.
- **Target API level** is 35 ✓ (Google requires 34+ as of Aug 2025).
- **Privacy policy URL** must be reachable from the listing — your `privacy-policy.html` needs to be hosted somewhere public (GitHub Pages on this repo works).

---

## File map

| Path | Purpose |
|---|---|
| `android/app/build.gradle.kts` | reads signing from env vars or `keystore.properties` |
| `android/keystore.properties.example` | template for local signing |
| `android/Gemfile` | locks Fastlane version |
| `android/fastlane/Appfile` | Play package + service account path |
| `android/fastlane/Fastfile` | `build` / `internal` / `promote_to_production` / `release` lanes |
| `.github/workflows/release.yml` | tag-triggered CI pipeline |
| `~/.config/finbaby/` | **local, never commit** — keystore + service account key + creds |
