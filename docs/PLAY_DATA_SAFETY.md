# Google Play Data Safety — Sushi Clash checklist

Practical guide for completing the **Data safety** form in Google Play Console for Sushi Clash, based on the current implementation (version 1.0, `com.mancebolabs.sushiclash`).

Use this together with [Google Play’s Data safety guidance](https://support.google.com/googleplay/android-developer/answer/10787469).

**Important distinction used throughout this document:**

| Term | Meaning for Sushi Clash |
|------|-------------------------|
| **Collected by Mancebo Labs** | Transmitted off the user’s device to Mancebo Labs or a third party at Mancebo Labs’ direction |
| **Stored locally** | Kept in on-device app storage only |
| **OS-managed backup** | Copied by Android/Google backup or device transfer; **not accessed by Mancebo Labs** |

Do **not** describe OS backup as data “collected by Mancebo Labs.” Do explain it in the **Privacy Policy** and, where the form allows, in the **store listing / policy declarations**.

---

## Pre-flight audit summary (verified in code)

| Area | Finding |
|------|---------|
| Permissions | **None** declared in `AndroidManifest.xml` |
| Network / APIs | **No** HTTP clients or Mancebo Labs backend |
| Analytics / crash SDKs | **None** (no Firebase, Crashlytics, etc.) |
| Accounts / login | **None** |
| Ads | **None** |
| Local persistence | Single DataStore file + AppCompat language SharedPreferences |
| Android backup | **Enabled**; explicit include-only rules for preferences + language |
| Third-party runtime SDK | AndroidX + **Google Fonts via Google Play services** (font downloads only) |
| In-app deletion | Clear history; Clear achievements (partial) |

---

## Section 1 — Top-level questions

### Does your app collect or share any of the required user data types?

**Recommended answer: No**

**Why:** Under Google Play’s definition, “collected” means data transmitted off the user’s device to the developer or a third party. Sushi Clash stores user-entered names, scores, history, and settings **only on device**. Mancebo Labs operates **no servers** and embeds **no analytics or crash-reporting SDKs**.

**Manual verification / decision:**

- [ ] Confirm you agree that OS backup copies are **not** “collection by Mancebo Labs” (Google’s guidance: developers generally do not declare backup data they cannot access).
- [ ] If Google’s form wording changes or your legal counsel advises broader disclosure, revisit this answer before publishing.

---

### Is all of the user data collected by your app encrypted in transit?

**Recommended answer: N/A** (because you answered **No** to collection)

**Why:** Mancebo Labs does not transmit user game data.

**Separate note (not user data):** Google Play services may download fonts over encrypted connections. That is not transmission of the user’s game data or personal information from the app.

---

### Do you provide a way for users to request that their data is deleted?

**Recommended answer: Yes**

**Why:** Users can delete stored data on device:

| Method | What it removes |
|--------|-----------------|
| Settings → **Clear history** | Solo and Group saved games |
| Settings → **Clear achievements** | Achievement progress and unlocks |
| Android **Clear app data** / uninstall | **All** app data (history, achievements, frequent players, settings, active game) |

**Store-listing / policy text suggestion:** Explain partial in-app deletion and that full deletion requires clearing app data or uninstalling. See `PRIVACY_POLICY.md`.

**Manual verification / decision:**

- [ ] Decide whether partial in-app deletion is sufficient for your jurisdiction and Play policies, or whether you want to add a future “Delete all app data” control before launch.

---

## Section 2 — Data types (detailed declarations)

Because the recommended top-level answer is **No data collected**, you typically **do not declare individual data types as collected or shared**.

Below is a **reference matrix** if Google asks follow-up questions, your counsel recommends disclosure, or form requirements change.

| Play Console category | Examples in Sushi Clash | Stored locally? | Collected by Mancebo Labs? | Shared with third parties? |
|-----------------------|-------------------------|-----------------|----------------------------|----------------------------|
| Personal info → Name | Optional player names in Group mode | Yes | **No** | **No** |
| App activity → Other user-generated content | Game history, scores, roulette participants | Yes | **No** | **No** |
| App info and performance | Not used for telemetry | — | **No** | **No** |
| Device or other IDs | Not used | — | **No** | **No** |
| Location | Not used | — | **No** | **No** |
| Financial info | Not used | — | **No** | **No** |
| Photos and videos | Not used | — | **No** | **No** |
| Contacts | Not used | — | **No** | **No** |
| Messages | Not used | — | **No** | **No** |

### Achievements

Achievement state contains **aggregated counters and unlock timestamps**, not player names. Same conclusion: local only, not collected by Mancebo Labs.

### Frequent players

Stored locally; updated when a Group game is **saved** at finish. Not cleared by “Clear history” or “Clear achievements” alone — only by full app data clear.

---

## Section 3 — Data usage and handling (if form still asks)

If any section remains applicable despite “No collection,” use these answers:

| Question | Recommended answer | Notes |
|----------|-------------------|-------|
| Purpose: App functionality | Yes (on device only) | Counting, history, achievements, settings |
| Purpose: Analytics | **No** | No analytics SDK |
| Purpose: Advertising | **No** | No ads |
| Purpose: Fraud prevention / security | **No** | Not applicable |
| Purpose: Personalization | **No** | Theme/language are local preferences, not remote profiling |
| Data sold | **No** | — |
| Data required vs optional | Optional | Player names are optional labels for local play |

---

## Section 4 — Security practices

| Question | Recommended answer | Why |
|----------|-------------------|-----|
| Data encrypted in transit (user data) | **N/A / No** | User data is not transmitted by the app |
| Data encrypted at rest | Follow form options carefully | The app relies on **Android app-private storage**, not app-level encryption. Do **not** claim custom encryption unless implemented. Many developers select “Yes” only if the form counts Android device encryption; **verify the exact wording in Console**. |
| Users can request deletion | **Yes** | See Section 1 |

**Manual verification / decision:**

- [ ] Read the exact Play Console options for “encrypted at rest” and choose the answer that matches your legal review. The app does **not** implement its own encryption layer.

---

## Section 5 — Third-party SDKs and libraries

Declare in **App content → Data safety → Third-party SDKs** (if prompted) based on what is actually bundled:

| Library | Purpose | Sends user game data? |
|---------|---------|----------------------|
| AndroidX (Core, AppCompat, Lifecycle, Compose, Navigation, DataStore, Material) | UI and local storage | **No** |
| `androidx.compose.ui:ui-text-google-fonts` | Download/display fonts via Google Play services | **No** user game data; technical font requests only |
| Desugar JDK libs | Language compatibility | **No** |

**Recommended:** Do **not** list Firebase, Crashlytics, Ads, or Play Billing — they are not in the project.

**Manual verification / decision:**

- [ ] After dependency updates, re-run this checklist before each release.
- [ ] If you add crash reporting or analytics later, **update Data safety and `PRIVACY_POLICY.md` before publishing**.

---

## Section 6 — Android backup (policy vs Console form)

### What is backed up

From `backup_rules.xml` and `data_extraction_rules.xml`:

| Included in cloud backup & device transfer | Contents |
|--------------------------------------------|----------|
| `datastore/sushi_counter_preferences.preferences_pb` | History, achievements, frequent players, settings, active game fields, etc. |
| `androidx.appcompat.app.AppCompatDelegate.application_locales` | Language override |

| Excluded | Contents |
|----------|----------|
| `files/no_backup/install_marker` | Install/restore detection marker (empty file) |

### Restore behavior

On first launch after restore, **in-progress games are cleared** (`clearActiveGameAfterBackupRestoreIfNeeded`). History and achievements are kept.

### How to describe this

| Where | What to say |
|-------|-------------|
| **Privacy Policy** | Backup may copy app data to Google/device-managed backup; Mancebo Labs does not access it |
| **Data safety form** | Generally **do not** mark as “collected by developer” if you cannot access backups |
| **Play listing (optional)** | “Data is stored on your device; Android backup may apply if enabled in your system settings” |

**Manual verification / decision:**

- [ ] Confirm your Google account backup settings on a test device if you want to validate end-to-end restore behavior before launch.
- [ ] Decide whether marketing copy should mention backup explicitly (optional, not required by code).

---

## Section 7 — Permissions

**Recommended:** No sensitive permissions to declare — the manifest contains **zero** `<uses-permission>` entries.

| Capability | How it works |
|------------|--------------|
| Vibration | Uses system vibrator APIs; no `VIBRATE` permission declared (valid on supported API levels) |
| Sound | Local preference only; no microphone |
| Internet | **Not declared**; font downloads go through Google Play services |

---

## Section 8 — Privacy Policy URL (Play Console)

Before publishing, you must host `PRIVACY_POLICY.md` at a **public HTTPS URL** and enter it in Play Console.

**Manual verification / decision — required before publish:**

- [ ] Replace `[CONTACT EMAIL]` in `PRIVACY_POLICY.md`
- [ ] Replace `[EFFECTIVE DATE]`
- [ ] Publish policy (GitHub Pages, website, Notion public page, etc.)
- [ ] Enter URL in **Play Console → App content → Privacy policy**
- [ ] Optionally add an in-app link in Settings ( **not implemented today** — product decision)

---

## Section 9 — Other Play Console items (related, not Data safety)

| Item | Sushi Clash status | Action |
|------|-------------------|--------|
| Account deletion | No accounts | Declare “No account required” where applicable |
| Target audience / Designed for families | Not assessed in code | **You decide** based on marketing and content |
| Government apps | No | No |
| Financial features | No | No |
| Health features | No | No |
| COVID-19 apps | No | No |

---

## Section 10 — Release checklist (copy before each submission)

```
[ ] PRIVACY_POLICY.md published at public URL with real email and date
[ ] Play Console Data safety: "No" collection / sharing (unless legal review says otherwise)
[ ] Deletion path documented (in-app partial + system clear data)
[ ] Third-party SDK list matches build.gradle.kts
[ ] No undeclared analytics/crash SDKs added since last audit
[ ] Backup wording in Privacy Policy matches backup_rules.xml / data_extraction_rules.xml
[ ] Store listing does NOT claim "data never leaves device" without backup caveat
[ ] mapping.txt archived for release builds (operational, not Data safety)
```

---

## Quick reference — what Sushi Clash actually stores

| Data | Location | In Android backup? | Deletable in app? |
|------|----------|--------------------|-------------------|
| Player names (Group) | DataStore (`players`, `participants`, `group_history`, `frequent_players`) | Yes | History only via Clear history; names in frequent players via system clear data |
| Solo / Group history | DataStore | Yes | Clear history |
| Achievements | DataStore | Yes | Clear achievements |
| Theme, sound, vibration, onboarding | DataStore | Yes | System clear data only |
| Language | AppCompat SharedPreferences | Yes | System clear data only |
| Active game | DataStore | Yes (cleared on first launch after restore) | Finish game / restore logic |

---

## Items that require your manual decision before publishing

1. **Contact email** for Privacy Policy and Play Console developer contact.
2. **Effective date** for the Privacy Policy.
3. **Hosting URL** for the privacy policy.
4. **Legal review** of “No data collected” vs jurisdictional disclosure expectations (EU/UK/CH etc.).
5. **Encrypted at rest** checkbox — confirm against exact Play Console wording and counsel.
6. **Target audience / age rating** — independent of Data safety but affects store compliance.
7. **Whether to add** an in-app privacy policy link or “Delete all data” button before v1.0 launch.
8. **Re-audit** after any new dependency that uses network, accounts, analytics, or cloud storage.

---

*Last audited against the Sushi Clash codebase on 29 August 2026. Update this document when persistence, backup rules, permissions, or dependencies change.*
